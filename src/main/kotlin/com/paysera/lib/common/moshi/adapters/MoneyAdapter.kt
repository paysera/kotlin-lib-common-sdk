package com.paysera.lib.common.moshi.adapters

import com.paysera.lib.common.moshi.entities.MoneyJson
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import org.joda.money.CurrencyUnit
import org.joda.money.Money

class MoneyAdapter {

    @ToJson
    fun toJson(money: Money?): MoneyJson {
        return MoneyJson(money?.amount, money?.currencyUnit?.currencyCode)
    }

    @FromJson
    fun fromJson(money: MoneyJson): Money {
        return Money.of(CurrencyUnit.of(money.currency), money.amount)
    }
}