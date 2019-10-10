package com.paysera.lib.common.entities

import com.google.gson.annotations.SerializedName

data class MetadataAwareResponse <T> (
    val items: List<T>,
    @SerializedName("_metadata")
    val metadata: Metadata
)