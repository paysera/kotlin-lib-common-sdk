package com.paysera.lib.common.jwt

import java.util.Date

interface Claim {
    fun asBoolean(): Boolean?
    fun asInt(): Int?
    fun asLong(): Long?
    fun asDouble(): Double?
    fun asString(): String?
    fun asDate(): Date?
    @Throws(DecodeException::class)
    fun <T> asList(tClazz: Class<T>): List<T>
}