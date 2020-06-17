package com.paysera.lib.common.moshi.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.math.BigDecimal

class BigDecimalAdapter {

    @ToJson
    fun toJson(value: BigDecimal) = value.toString()

    @FromJson
    fun fromJson(string: String) = BigDecimal(string)
}