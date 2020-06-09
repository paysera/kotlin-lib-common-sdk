package com.paysera.lib.common.serializers.entities

import java.math.BigDecimal

data class MoneyJson(
    val amount: BigDecimal,
    val currency: String
)