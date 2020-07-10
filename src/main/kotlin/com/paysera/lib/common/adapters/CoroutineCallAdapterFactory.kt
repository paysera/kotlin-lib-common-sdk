package com.paysera.lib.common.adapters

import com.google.gson.Gson
import com.paysera.lib.common.exceptions.ApiError
import com.paysera.lib.common.interfaces.ErrorLoggerInterface
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import retrofit2.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class CoroutineCallAdapterFactory private constructor(
    private val errorLogger: ErrorLoggerInterface
): CallAdapter.Factory() {

    companion object {
        @JvmStatic @JvmName("create")
        operator fun invoke(
            errorLogger: ErrorLoggerInterface
        ) = CoroutineCallAdapterFactory(errorLogger)
    }

    private val gson = Gson()

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

            makeRequest(CallAdapterRequest(call, deferred))

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

            makeRequest(CallAdapterRequest(call, deferred, true))

            return deferred
        }
    }

    private fun makeRequest(request: CallAdapterRequest) {
        request.call.enqueue(object : Callback<Any> {
            override fun onFailure(call: Call<Any>, t: Throwable) {
                errorLogger.log(call.request(), t)
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
                    mapError(response).also {
                        errorLogger.log(call.request(), it)
                        request.deferred.completeExceptionally(it)
                    }
                }
            }
        })
    }

    private fun mapError(response: Response<Any>): ApiError {
        val responseString = response.errorBody()?.string() ?: return ApiError.unknown()
        return try {
            gson.fromJson(responseString, ApiError::class.java).also {
                it.statusCode = response.code()
            }
        } catch (e: Throwable) {
            ApiError(message = responseString).also {
                it.statusCode = response.code()
            }
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
}