package com.paysera.lib.common.moshi.adapters

import com.paysera.lib.common.exceptions.ApiError
import com.paysera.lib.common.moshi.entities.ApiErrorJson
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

class ApiErrorAdapter {

    @ToJson
    fun toJson(apiError: ApiError?): ApiErrorJson {
        return ApiErrorJson(
            apiError?.error,
            apiError?.description,
            apiError?.statusCode,
            apiError?.properties,
            apiError?.errorFields,
            apiError?.data
        )
    }

    @FromJson
    fun fromJson(apiError: ApiErrorJson): ApiError {
        val error = ApiError(apiError.error, apiError.description, apiError.statusCode)
        error.apply {
            properties = apiError.properties
            errorFields = apiError.errorFields
            data = apiError.data
        }
        return error
    }
}