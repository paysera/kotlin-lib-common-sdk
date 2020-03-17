package com.paysera.lib.common.entities

import com.paysera.lib.common.jwt.JWT
import java.lang.Exception
import java.util.*

class ApiCredentials constructor(
    token: String?,
    private val expirationLeeway: Long
) {

    var token: String? = null
        set(value) {
            decodeJWT(value)
            field = value
        }
        get() {
            return jwt?.toString()
        }

    private var jwt: JWT? = null

    init {
        decodeJWT(token)
    }

    fun hasExpired(): Boolean {
        val expirationTime = jwt?.expiresAt?.time
        if (expirationTime != null) {
            return expirationTime.minus(Date().time) < expirationLeeway
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
        token ?: return
        try {
            jwt = JWT(token)
        } catch (e: Exception) { }
    }
}