package com.paysera.lib.common.exceptions

import com.google.gson.annotations.SerializedName
import java.lang.Exception
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ApiError : Exception {

    var error: String? = null
    @SerializedName("error_description")
    var description: String? = null
    @SerializedName("status_code")
    var statusCode: Int? = null
    @SerializedName("error_properties")
    var properties: List<ApiErrorProperty>? = null
    @SerializedName("errors")
    var errorFields: List<ApiErrorField>? = null
    @SerializedName("error_data")
    var data: Any? = null

    constructor(error: String? = null, description: String? = null, statusCode: Int? = null) {
        this.error = error
        this.description = description
        this.statusCode = statusCode
    }

    constructor(message: String, error: Throwable?) : super(message, error)

    constructor(message: String) : super(message)

    constructor(cause: Throwable?) : super(cause)
    
    fun isNoInternet(): Boolean {
        if (cause is UnknownHostException || cause is SocketTimeoutException || cause is SocketException) {
            return true
        }
        return error == "no_internet"
    }

    fun isQueueOverflow(): Boolean {
        return error == "queue_overflow"
    }

    fun isUnauthorized(): Boolean {
        return error == "unauthorized"
    }

    fun isRefreshTokenExpired(): Boolean {
        return error == "invalid_grant"
            && (description == "Refresh token expired" || description == "No such refresh token")
    }

    fun isTokenExpired(): Boolean {
        return error == "invalid_grant" && description == "Token has expired"
    }

    fun isInvalidTimestamp(): Boolean {
        return error == "invalid_timestamp"
    }

    fun isRateLimitExceeded(): Boolean {
        return error == "rate_limit_exceeded"
    }

    companion object {

        fun noInternet(): ApiError {
            return ApiError("no_internet")
        }

        fun unauthorized(): ApiError {
            return ApiError("unauthorized")
        }

        fun unknown(): ApiError {
            return ApiError("unknown")
        }

        fun cancelled(): ApiError {
            return ApiError("cancelled")
        }

        fun queueOverflow(): ApiError {
            return ApiError("queue_overflow")
        }
    }
}