package com.paysera.lib.common.interfaces

interface BaseApiCredentials {
    var token: String?
    fun hasExpired(): Boolean
    fun hasRecentlyRefreshed(): Boolean
}