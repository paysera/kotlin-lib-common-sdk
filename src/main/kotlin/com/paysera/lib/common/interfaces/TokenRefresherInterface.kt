package com.paysera.lib.common.interfaces

import kotlinx.coroutines.Deferred

interface TokenRefresherInterface {
    fun refreshToken(): Deferred<Any>
}