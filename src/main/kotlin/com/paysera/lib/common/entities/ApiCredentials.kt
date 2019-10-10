package com.paysera.lib.common.entities

import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import java.lang.Exception
import java.util.*

class ApiCredentials constructor(token: String?) {

    var token: String? = null
        set(value) {
            decodeJWT(value)
            field = value
        }
        get() {
            return jwt?.token
        }

    private var jwt: DecodedJWT? = null

    init {
        decodeJWT(token)
    }

    fun hasExpired(): Boolean {
        val expirationTime = jwt?.expiresAt?.time
        if (expirationTime != null) {
            return expirationTime.minus(Date().time) < 120 * 1000
        }
        return true
    }

    fun hasRecentlyRefreshed(): Boolean {
        val issuanceTime = jwt?.issuedAt?.time
        if (issuanceTime != null) {
            return Date().time.minus(issuanceTime) < 15 * 1000
        }
        return false
    }

    private fun decodeJWT(token: String?) {
        try {
            jwt = JWT.decode(token)
        } catch (e: Exception) { }
    }
}