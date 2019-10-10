package com.paysera.lib.common.exceptions

data class ApiErrorField(
    val code: String,
    val field: String,
    val message: String
)