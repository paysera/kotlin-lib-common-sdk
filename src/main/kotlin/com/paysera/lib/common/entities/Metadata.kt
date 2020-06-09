package com.paysera.lib.common.entities

import com.squareup.moshi.Json

data class Metadata(
    val total: Int,
    val limit: Int,
    val offset: Int,
    @Json(name = "has_next")
    val hasNext: Boolean,
    @Json(name = "has_previous")
    val hasPrevious: Boolean
)