package com.paysera.lib.common.jwt

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonSyntaxException
import java.util.Date

class ClaimImpl(private val value: JsonElement) : BaseClaim() {

    override fun asBoolean(): Boolean? {
        return if (!value.isJsonPrimitive) {
            null
        } else value.asBoolean
    }

    override fun asInt(): Int? {
        return if (!value.isJsonPrimitive) {
            null
        } else value.asInt
    }

    override fun asLong(): Long? {
        return if (!value.isJsonPrimitive) {
            null
        } else value.asLong
    }

    override fun asDouble(): Double? {
        return if (!value.isJsonPrimitive) {
            null
        } else value.asDouble
    }

    override fun asString(): String? {
        return if (!value.isJsonPrimitive) {
            null
        } else value.asString
    }

    override fun asDate(): Date? {
        if (!value.isJsonPrimitive) {
            return null
        }
        val ms = java.lang.Long.parseLong(value.asString) * 1000
        return Date(ms)
    }

    @Throws(DecodeException::class)
    override fun <T> asList(tClazz: Class<T>): List<T> {
        try {
            if (!value.isJsonArray || value.isJsonNull) {
                return emptyList()
            }
            val gson = Gson()
            val jsonArr = value.asJsonArray
            val list = mutableListOf<T>()
            for (i in 0 until jsonArr.size()) {
                list.add(gson.fromJson(jsonArr.get(i), tClazz))
            }
            return list
        } catch (e: JsonSyntaxException) {
            throw DecodeException("Failed to decode claim as list", e)
        }

    }
}