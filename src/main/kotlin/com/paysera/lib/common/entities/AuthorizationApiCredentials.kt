package com.paysera.lib.common.entities

import com.paysera.lib.common.interfaces.BaseApiCredentials
import com.paysera.lib.common.jwt.JWT
import java.util.*

class AuthorizationApiCredentials constructor(
    token: String?,
    private val expirationLeeway: Long
) : BaseApiCredentials {

    override var token: String? = null
        set(value) {
            decodeJWT(value)
            field = value
        }
        get() {
            return "Bearer %s".format(jwt?.toString())
        }

    private var jwt: JWT? = null

    init {
        decodeJWT(token)
    }

    override var headerKey = "Authorization"

    override fun hasExpired(): Boolean {
        val expirationTime = jwt?.expiresAt?.time
        if (expirationTime != null) {
            return expirationTime.minus(Date().time) < expirationLeeway
        }
        return true
    }

    override fun hasRecentlyRefreshed(): Boolean {
        val issuanceTime = jwt?.issuedAt?.time
        if (issuanceTime != null) {
            return Date().time.minus(issuanceTime) < 15 * 1000
        }
        return false
    }

    private fun decodeJWT(token: String?) {
        token ?: return
        try {
            jwt = JWT(token)
        } catch (e: Exception) { }
    }
}