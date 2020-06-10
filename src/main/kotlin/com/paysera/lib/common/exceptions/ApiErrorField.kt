package com.paysera.lib.common.exceptions

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiErrorField(
    val code: String,
    val field: String,
    val message: String
)