package com.paysera.lib.common.serializers

import com.google.gson.*
import org.joda.money.CurrencyUnit
import org.joda.money.Money
import java.lang.reflect.Type

class MoneySerializer : JsonDeserializer<Money>, JsonSerializer<Money> {

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Money {
        val jsonObject = json?.asJsonObject
        val amount = jsonObject?.get("amount")?.asBigDecimal
        val currency = jsonObject?.get("currency")?.asString
        return Money.of(CurrencyUnit.of(currency), amount)
    }

    override fun serialize(src: Money?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        val amountObject = JsonObject()
        amountObject.addProperty("amount", src?.amount?.toPlainString())
        amountObject.addProperty("currency", src?.currencyUnit?.currencyCode)
        return amountObject
    }
}