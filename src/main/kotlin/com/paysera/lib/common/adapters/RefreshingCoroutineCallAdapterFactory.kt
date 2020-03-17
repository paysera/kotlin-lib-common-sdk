package com.paysera.lib.common.adapters

import com.google.gson.Gson
import com.paysera.lib.common.entities.ApiCredentials
import com.paysera.lib.common.exceptions.ApiError
import com.paysera.lib.common.interfaces.CancellableAdapterFactory
import com.paysera.lib.common.interfaces.TokenRefresherInterface
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import retrofit2.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class RefreshingCoroutineCallAdapterFactory private constructor(
    private val credentials: ApiCredentials,
    private val tokenRefresher: TokenRefresherInterface
) : CallAdapter.Factory(), CancellableAdapterFactory {

    companion object {
        @JvmStatic @JvmName("create")
        operator fun invoke(
            credentials: ApiCredentials,
            tokenRefresher: TokenRefresherInterface
        ) = RefreshingCoroutineCallAdapterFactory(credentials, tokenRefresher)
    }

    private val gson = Gson()
    private val requestQueue = mutableListOf<CallAdapterRequest>()

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

            synchronized(tokenRefresher) {
                when {
                    tokenRefresher.isRefreshing() -> {
                        requestQueue.add(CallAdapterRequest(call.clone(), deferred))
                    }
                    credentials.hasExpired() -> {
                        requestQueue.add(CallAdapterRequest(call.clone(), deferred))
                        refreshToken()
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

            synchronized(tokenRefresher) {
                when {
                    tokenRefresher.isRefreshing() -> {
                        requestQueue.add(CallAdapterRequest(call.clone(), deferred, true))
                    }
                    credentials.hasExpired() -> {
                        requestQueue.add(CallAdapterRequest(call.clone(), deferred, true))
                        refreshToken()
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
                        synchronized(tokenRefresher) {
                            if (credentials.hasRecentlyRefreshed()) {
                                makeRequest(request.clone())
                            } else {
                                requestQueue.add(request.clone())
                                if (!tokenRefresher.isRefreshing()) {
                                    refreshToken()
                                }
                            }
                        }
                    } else {
                        request.deferred.completeExceptionally(mapError(response))
                    }
                }
            }
        })
    }

    private fun refreshToken() {
        tokenRefresher.refreshToken().invokeOnCompletion { error ->
            synchronized(tokenRefresher) {
                if (error != null) {
                    cancelQueue(ApiError.unauthorized())
                } else {
                    resumeQueue()
                }
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
            it.deferred.completeExceptionally(error)
        }
        requestQueue.clear()
    }

    private fun mapError(response: Response<Any>): ApiError {
        val responseString = response.errorBody()?.string() ?: return ApiError.unknown()
        val error = gson.fromJson<ApiError>(responseString, ApiError::class.java)
        error.statusCode = response.code()
        return error
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