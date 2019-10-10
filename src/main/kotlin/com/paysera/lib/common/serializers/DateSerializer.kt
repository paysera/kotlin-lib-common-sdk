package com.paysera.lib.common.serializers

import com.google.gson.*
import java.lang.reflect.Type
import java.text.ParseException
import java.util.*

class DateSerializer : JsonDeserializer<Date>, JsonSerializer<Date> {

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Date {
        val time = json?.asJsonPrimitive?.asLong
        time?.let {
            return Date(it * 1000L)
        }
        throw ParseException("Failed to parse date", -1)
    }

    override fun serialize(src: Date?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive((src?.time?.div(1000))?.toInt())
    }
}