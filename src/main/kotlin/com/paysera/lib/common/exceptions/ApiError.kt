package com.paysera.lib.common.exceptions

import com.squareup.moshi.Json
import java.lang.Exception

class ApiError : Exception {

    var error: String? = null
    @Json(name = "error_description")
    var description: String? = null
    @Json(name = "status_code")
    var statusCode: Int? = null
    @Json(name = "error_properties")
    var properties: List<ApiErrorProperty>? = null
    @Json(name = "errors")
    var errorFields: List<ApiErrorField>? = null
    @Json(name = "error_data")
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