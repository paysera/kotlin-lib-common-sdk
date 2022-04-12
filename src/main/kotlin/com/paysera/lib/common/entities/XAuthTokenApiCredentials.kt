package com.paysera.lib.common.entities

import com.paysera.lib.common.interfaces.BaseApiCredentials
import java.util.*

class XAuthTokenApiCredentials(
    var accessTokenExpiresAt: Date? = null,
    var accessTokenIssuedAt: Date? = null,
    override var token: String? = null,
    val xApiKey: String? = null
) : BaseApiCredentials {

    var locale: String = "en"

    private val timeNowInMills: Long
        get() {
            return Calendar.getInstance().time.time / 1000
        }

    override fun hasExpired(): Boolean {
        if (accessTokenExpiresAt == accessTokenIssuedAt) {
            return false
        }

        val accessTokenExpiresAtMills = accessTokenExpiresAt?.time ?: return false
        return (accessTokenExpiresAtMills / 1000 - timeNowInMills) < 120
    }

    override fun hasRecentlyRefreshed(): Boolean {
        val accessTokenIssuedAtMills = accessTokenIssuedAt?.time ?: return false
        return (accessTokenIssuedAtMills / 1000 - timeNowInMills) < 15
    }
}