package com.paysera.lib.common.jwt

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.UnsupportedEncodingException
import java.lang.reflect.Type
import java.nio.charset.StandardCharsets
import java.util.Date

class JWT(private val token: String) {

    var header: Map<String, String>? = null
        private set
    private var payload: JWTPayload? = null

    var signature: String? = null
        private set

    val issuer: String?
        get() = payload?.iss

    val subject: String?
        get() = payload?.sub

    val audience: List<String>?
        get() = payload?.aud

    val expiresAt: Date?
        get() = payload?.exp

    val notBefore: Date?
        get() = payload?.nbf

    val issuedAt: Date?
        get() = payload?.iat

    val id: String?
        get() = payload?.jti

    val claims: Map<String, Claim>
        get() = payload!!.tree

    init {
        decode(token)
    }

    fun getClaim(name: String): Claim {
        return payload!!.claimForName(name)
    }

    fun isExpired(leeway: Long): Boolean {
        require(leeway >= 0) { "The leeway must be a positive value." }
        val todayTime = (Math.floor((Date().time / 1000).toDouble()) * 1000).toLong() //truncate millis
        val futureToday = Date(todayTime + leeway * 1000)
        val pastToday = Date(todayTime - leeway * 1000)
        val expValid = payload?.exp == null || !pastToday.after(payload!!.exp)
        val iatValid = payload?.iat == null || !futureToday.before(payload!!.iat)
        return !expValid || !iatValid
    }

    override fun toString(): String {
        return token
    }

    fun describeContents(): Int {
        return 0
    }

    private fun decode(token: String) {
        val parts = splitToken(token)
        val mapType = object : TypeToken<Map<String, String>>() {

        }.type
        header = parseJson<Map<String, String>>(base64Decode(parts[0]), mapType)
        payload = parseJson<JWTPayload>(base64Decode(parts[1]), JWTPayload::class.java)
        signature = parts[2]
    }

    private fun splitToken(token: String): Array<String> {
        var parts = token.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (parts.size == 2 && token.endsWith(".")) {
            //Tokens with alg='none' have empty String as Signature.
            parts = arrayOf(parts[0], parts[1], "")
        }
        if (parts.size != 3) {
            throw DecodeException(String.format("The token was expected to have 3 parts, but got %s.", parts.size))
        }
        return parts
    }

    private fun base64Decode(string: String): String {
        val decoded: String
        try {
            val bytes = Base64.decode(string)
            decoded = String(bytes, StandardCharsets.UTF_8)
        } catch (e: IllegalArgumentException) {
            throw DecodeException("Received bytes didn't correspond to a valid Base64 encoded string.", e)
        } catch (e: UnsupportedEncodingException) {
            throw DecodeException("Device doesn't support UTF-8 charset encoding.", e)
        }

        return decoded
    }

    private fun <T> parseJson(json: String, typeOfT: Type): T? {
        val payload: T
        try {
            payload = gson.fromJson(json, typeOfT)
        } catch (e: Exception) {
            throw DecodeException("The token's payload had an invalid JSON format.", e)
        }

        return payload
    }

    companion object {

        internal val gson: Gson
            get() = GsonBuilder()
                .registerTypeAdapter(JWTPayload::class.java, JWTDeserializer())
                .create()
    }
}