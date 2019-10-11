package com.paysera.lib.common.jwt

import java.util.Date

open class BaseClaim : Claim {

    override fun asBoolean(): Boolean? {
        return null
    }

    override fun asInt(): Int? {
        return null
    }

    override fun asLong(): Long? {
        return null
    }

    override fun asDouble(): Double? {
        return null
    }

    override fun asString(): String? {
        return null
    }

    override fun asDate(): Date? {
        return null
    }

    @Throws(DecodeException::class)
    override fun <T> asList(tClazz: Class<T>): List<T> {
        return emptyList()
    }
}