package com.paysera.lib.common.moshi.entities

import com.paysera.lib.common.exceptions.ApiErrorField
import com.paysera.lib.common.exceptions.ApiErrorProperty
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class ApiErrorJson(
    var error: String? = null,
    @Json(name = "error_description")
    var description: String? = null,
    @Json(name = "status_code")
    var statusCode: Int? = null,
    @Json(name = "error_properties")
    var properties: List<ApiErrorProperty>? = null,
    @Json(name = "errors")
    var errorFields: List<ApiErrorField>? = null,
    @Json(name = "error_data")
    var data: Any? = null
)