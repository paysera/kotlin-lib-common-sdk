package com.paysera.lib.common.entities

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
open class BaseFilter(
    var offset: Int? = null,
    var limit: Int? = null,
    @Json(name = "order_by")
    var orderBy: String? = null,
    @Json(name = "order_direction")
    var orderDirection: String? = null,
    var after: String? = null,
    var before: String? = null
)