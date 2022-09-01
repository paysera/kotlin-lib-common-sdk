package com.paysera.lib.common.interfaces

interface RefreshingApiCredentials  {
    fun hasExpired(): Boolean
    fun hasRecentlyRefreshed(): Boolean
}