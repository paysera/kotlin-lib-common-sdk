package com.paysera.lib.common.moshi.entities

import com.squareup.moshi.JsonClass
import java.math.BigDecimal

@JsonClass(generateAdapter = true)
data class MoneyJson(
    val amount: BigDecimal,
    val currency: String
)