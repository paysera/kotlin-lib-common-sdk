package com.paysera.lib.common.entities

data class Metadata(
    val total: Int,
    val limit: Int,
    val offset: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)