package com.paysera.lib.common.exceptions

import java.lang.Exception

class ApiError : Exception {

    var error: String? = null
    var description: String? = null
    var statusCode: Int? = null
    var properties: List<ApiErrorProperty>? = null
    var errorFields: List<ApiErrorField>? = null
    var data: Any? = null

    constructor(error: String? = null, description: String? = null, statusCode: Int? = null) {
        this.error = error
        this.description = description
        this.statusCode = statusCode
    }

    constructor(message: String, error: Throwable?) : super(message, error)


    fun isNoInternet(): Boolean {
        return error == "no_internet"
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
    }
}