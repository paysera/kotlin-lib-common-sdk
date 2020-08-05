package com.paysera.lib.common.jwt

import com.squareup.moshi.Moshi
import java.util.Date

class ClaimImpl(private val value: Any) : BaseClaim() {

    override fun asBoolean(): Boolean? {
        return value as? Boolean
    }

    override fun asInt(): Int? {
        return value as? Int
    }

    override fun asLong(): Long? {
        return value as? Long
    }

    override fun asDouble(): Double? {
        return value as? Double
    }

    override fun asString(): String? {
        return value as? String
    }

    override fun asDate(): Date? {
        if (value !is String) {
            return null
        }
        val ms = java.lang.Long.parseLong(value) * 1000
        return Date(ms)
    }

    @Throws(DecodeException::class)
    override fun <T> asList(tClazz: Class<T>): List<T> {
        try {
            if (value !is ArrayList<*>) {
                return emptyList()
            }
            val moshi = Moshi.Builder().build()
            val adapter = moshi.adapter(tClazz)
            val jsonArr = value
            val list = mutableListOf<T>()
            for (i in 0 until jsonArr.size) {
                list.add(adapter.fromJson(jsonArr.get(i) as String)!!)
            }
            return list
        } catch (e: Exception) {
            throw DecodeException("Failed to decode claim as list", e)
        }
    }
}