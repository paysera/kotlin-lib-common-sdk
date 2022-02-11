package com.paysera.lib.common.entities

import com.paysera.lib.common.interfaces.BaseApiCredentials
import java.util.*

class XAuthTokenApiCredentials(
    val accessTokenExpiresAt: Date? = null,
    val accessTokenIssuedAt: Date? = null,
    override var token: String? = null,
    val xApiKey: String? = null
) : BaseApiCredentials {

    var locale: String = "en"

    override var headerKey = "x-auth-token"

    override fun hasExpired(): Boolean {
        return false
    }

    override fun hasRecentlyRefreshed(): Boolean {
        return true
    }
}