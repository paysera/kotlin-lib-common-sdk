package com.paysera.lib.common.entities

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Metadata(
    val total: Int = 0,
    val limit: Int = 0,
    val offset: Int = 0,
    @Json(name = "has_next")
    val hasNext: Boolean? = false,
    @Json(name = "has_previous")
    val hasPrevious: Boolean? = false
)