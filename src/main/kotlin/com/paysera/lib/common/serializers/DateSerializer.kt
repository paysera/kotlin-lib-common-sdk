package com.paysera.lib.common.serializers

import com.google.gson.*
import java.lang.reflect.Type
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class DateSerializer : JsonDeserializer<Date>, JsonSerializer<Date> {

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Date {
        if (json?.isJsonPrimitive == true && json.asJsonPrimitive?.asLong != null) {
            return Date(json.asJsonPrimitive.asLong * 1000L)
        } else if (json != null) {
            getDateFromInRento(json)?.let {
                return it
            }
        }
        throw ParseException("Failed to parse date", -1)
    }

    override fun serialize(src: Date?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive((src?.time?.div(1000))?.toInt())
    }

    private fun getDateFromInRento(json: JsonElement): Date? {
        if (!json.isJsonObject) return null
        val date = json.asJsonObject?.get("date")?.asString ?: return null
        val timezone = json.asJsonObject?.get("timezone")?.asString ?: return null
        return Calendar.getInstance().apply {
            time = SimpleDateFormat("yyyy-MM-dd HH:mm").parse(date)
            timeZone = TimeZone.getTimeZone(timezone)
        }.time
    }
}