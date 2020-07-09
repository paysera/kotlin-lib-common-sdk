package com.paysera.lib.common.adapters

import com.google.gson.Gson
import com.paysera.lib.common.entities.ApiCredentials
import com.paysera.lib.common.exceptions.ApiError
import com.paysera.lib.common.interfaces.CancellableAdapterFactory
import com.paysera.lib.common.interfaces.LoggerInterface
import com.paysera.lib.common.interfaces.TokenRefresherInterface
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import retrofit2.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class RefreshingCoroutineCallAdapterFactory private constructor(
    private val credentials: ApiCredentials,
    private val tokenRefresher: TokenRefresherInterface,
    private val logger: LoggerInterface
) : CallAdapter.Factory(), CancellableAdapterFactory {

    companion object {
        @JvmStatic @JvmName("create")
        operator fun invoke(
            credentials: ApiCredentials,
            tokenRefresher: TokenRefresherInterface,
            logger: LoggerInterface
        ) = RefreshingCoroutineCallAdapterFactory(credentials, tokenRefresher, logger)
    }

    private val gson = Gson()
    private val requestQueue = mutableListOf<CallAdapterRequest>()
    private var isRefreshTokenProcessing = false

    private val bodyCallAdapter = object : CallAdapter<Any, Deferred<Any>> {

        var responseType: Type? = null

        override fun responseType() = responseType

        override fun adapt(call: Call<Any>): Deferred<Any> {
            val deferred = CompletableDeferred<Any>()

            deferred.invokeOnCompletion {
                if (deferred.isCancelled) {
                    call.cancel()
                }
            }

            synchronized(this@RefreshingCoroutineCallAdapterFactory) {
                when {
                    credentials.hasExpired() -> {
                        requestQueue.add(CallAdapterRequest(call.clone(), deferred))
                        invokeRefreshToken()
                    }
                    else -> makeRequest(CallAdapterRequest(call, deferred))
                }
            }

            return deferred
        }
    }

    private val responseCallAdapter = object : CallAdapter<Any, Deferred<Response<Any>>> {

        var responseType: Type? = null

        override fun responseType() = responseType

        override fun adapt(call: Call<Any>): Deferred<Response<Any>> {
            val deferred = CompletableDeferred<Response<Any>>()

            deferred.invokeOnCompletion {
                if (deferred.isCancelled) {
                    call.cancel()
                }
            }

            synchronized(this@RefreshingCoroutineCallAdapterFactory) {
                when {
                    credentials.hasExpired() -> {
                        requestQueue.add(CallAdapterRequest(call.clone(), deferred, true))
                        invokeRefreshToken()
                    }
                    else -> makeRequest(CallAdapterRequest(call, deferred, true))
                }
            }

            return deferred
        }
    }

    private fun makeRequest(request: CallAdapterRequest) {
        request.call.enqueue(object : Callback<Any> {
            override fun onFailure(call: Call<Any>, t: Throwable) {
                logger.log(call.request(), t)
                request.deferred.completeExceptionally(t)
            }
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                if (response.isSuccessful) {
                    @Suppress("UNCHECKED_CAST")
                    if (request.isResponse) {
                        (request.deferred as? CompletableDeferred<Response<Any>>)?.complete(response)
                    } else {
                        (request.deferred as? CompletableDeferred<Any>)?.complete(response.body()!!)
                    }
                } else {
                    val exception = HttpException(response)
                    val isUnauthorized = exception.code() == 401
                    if (isUnauthorized) {
                        synchronized(this@RefreshingCoroutineCallAdapterFactory) {
                            if (credentials.hasRecentlyRefreshed()) {
                                makeRequest(request.clone())
                            } else {
                                requestQueue.add(request.clone())
                                invokeRefreshToken()
                            }
                        }
                    } else {
                        mapError(response).also {
                            logger.log(call.request(), it)
                            request.deferred.completeExceptionally(it)
                        }
                    }
                }
            }
        })
    }

    private fun invokeRefreshToken() {
        if (isRefreshTokenProcessing) {
            return
        }
        isRefreshTokenProcessing = true

        tokenRefresher.refreshToken().invokeOnCompletion { error ->
            synchronized(this@RefreshingCoroutineCallAdapterFactory) {
                if (error != null) {
                    cancelQueue(ApiError.unauthorized())
                } else {
                    resumeQueue()
                }
                isRefreshTokenProcessing = false
            }
        }
    }

    private fun resumeQueue() {
        requestQueue.forEach {
            makeRequest(it)
        }
        requestQueue.clear()
    }

    private fun cancelQueue(error: Throwable) {
        requestQueue.forEach {
            logger.log(it.call.request(), error)
            it.deferred.completeExceptionally(error)
        }
        requestQueue.clear()
    }

    private fun mapError(response: Response<Any>): Throwable {
        val responseString = response.errorBody()?.string() ?: return ApiError.unknown()
        return try {
            gson.fromJson(responseString, ApiError::class.java).also {
                it.statusCode = response.code()
            }
        } catch (e: Throwable) {
            Throwable(responseString)
        }
    }

    override fun get(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        if (Deferred::class.java != getRawType(returnType)) {
            return null
        }

        check(returnType is ParameterizedType) {
            "Deferred return type must be parameterized as Deferred<Foo> or Deferred<out Foo>"
        }

        val responseType = getParameterUpperBound(0, returnType)
        val rawDeferredType = getRawType(responseType)

        return if (rawDeferredType == Response::class.java) {
            check(responseType is ParameterizedType) {
                "Response must be parameterized as Response<Foo> or Response<out Foo>"
            }
            responseCallAdapter.also {
                it.responseType = getParameterUpperBound(0, responseType)
            }
        } else {
            bodyCallAdapter.also {
                it.responseType = responseType
            }
        }
    }

    //  CancellableAdapterFactory

    override fun cancelCalls() {
        cancelQueue(ApiError.cancelled())
    }
}