package com.paysera.lib.common.serializers

import com.paysera.lib.common.serializers.entities.MoneyJson
import org.joda.money.CurrencyUnit
import org.joda.money.Money

class MoneyAdapter {

    fun fromJson(money: MoneyJson): Money {
        return Money.of(CurrencyUnit.of(money.currency), money.amount)
    }

    fun toJson(money: Money): MoneyJson {
        return MoneyJson(money.amount, money.currencyUnit.currencyCode)
    }
}