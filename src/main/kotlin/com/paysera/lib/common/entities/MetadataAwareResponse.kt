package com.paysera.lib.common.entities

import com.squareup.moshi.Json

data class MetadataAwareResponse <T> (
    val items: List<T>,
    @Json(name = "_metadata")
    val metadata: Metadata
)