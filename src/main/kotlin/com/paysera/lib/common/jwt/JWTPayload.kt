package com.paysera.lib.common.jwt

import java.util.Collections
import java.util.Date

internal class JWTPayload {

    val iss: String?
    val sub: String?
    val exp: Date?
    val nbf: Date?
    val iat: Date?
    val jti: String?
    val aud: List<String>?
    val tree: Map<String, Claim>

    constructor(iss: String?, sub: String?, exp: Date?, nbf: Date?, iat: Date?, jti: String?, aud: List<String>?, tree: Map<String, Claim>) {
        this.iss = iss
        this.sub = sub
        this.exp = exp
        this.nbf = nbf
        this.iat = iat
        this.jti = jti
        this.aud = aud
        this.tree = Collections.unmodifiableMap(tree)
    }

    fun claimForName(name: String): Claim {
        val claim = this.tree[name]
        return claim ?: BaseClaim()
    }
}