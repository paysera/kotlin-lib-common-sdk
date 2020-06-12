package com.paysera.lib.common.exceptions

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class ApiErrorProperty(
    var code: String?,
    var description: String?
) {

    override fun toString(): String {
        return String.format("code=%s, desc=%s", code, description)
    }
}