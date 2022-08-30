package com.paysera.lib.common.entities

import com.paysera.lib.common.interfaces.BaseApiCredentials
import java.util.*

class XAuthTokenApiCredentials(
    var accessTokenExpiresAt: Date? = null,
    var accessTokenIssuedAt: Date? = null,
    override var token: String? = null,
    val xApiKey: String? = null
) : BaseApiCredentials, RetryHandlerCredentials {

    var locale: String = "en"

    companion object {
        private const val MAX_RETRY_COUNT = 3
    }

    private val timeNowInMills: Long
        get() {
            return Calendar.getInstance().time.time / 1000
        }

    // RetryHandlerCredentials

    override var retryCount: Int = 0

    // BaseApiCredentials

    override fun hasExpired(): Boolean {
        if (accessTokenExpiresAt == accessTokenIssuedAt) {
            return false
        }

        val accessTokenExpiresAtMills = accessTokenExpiresAt?.time ?: return false
        return (accessTokenExpiresAtMills / 1000 - timeNowInMills) < 120
    }

    override fun hasRecentlyRefreshed(): Boolean {
        val accessTokenIssuedAtMills = accessTokenIssuedAt?.time ?: return false
        if (retryCount >= MAX_RETRY_COUNT) {
            return false
        }
        retryCount++
        return (accessTokenIssuedAtMills / 1000 - timeNowInMills) < 15
    }
}