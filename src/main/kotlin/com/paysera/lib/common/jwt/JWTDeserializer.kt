package com.paysera.lib.common.jwt

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import java.util.Date
import java.util.HashMap

internal class JWTDeserializer {

    @FromJson
    fun fromJson(data: Map<String, Any>): JWTPayload {

        //Public Claims
        val iss = getString(data, "iss")
        val sub = getString(data, "sub")
        val exp = getDate(data, "exp")
        val nbf = getDate(data, "nbf")
        val iat = getDate(data, "iat")
        val jti = getString(data, "jti")
        val aud = getStringOrArray(data, "aud")

        //Private Claims
        val extra = HashMap<String, Claim>()
        for ((key, value) in data.entries) {
            extra[key] = ClaimImpl(value)
        }

        return JWTPayload(iss, sub, exp, nbf, iat, jti, aud, extra)
    }

    @ToJson
    fun toJson(writer: JsonWriter, data: JWTPayload) {}

    private fun getStringOrArray(data: Map<String, Any>, claimName: String): List<String> {
        var list = mutableListOf<String>()
        data[claimName]?.let {
            val arrElement = data[claimName]
            if (arrElement is ArrayList<*>) {
                list = mutableListOf()
                for (i in 0 until arrElement.size) {
                    list.add(arrElement[i] as String)
                }
            } else {
                list = mutableListOf(arrElement as String)
            }
        }
        return list
    }

    private fun getDate(data: Map<String, Any>, claimName: String): Date? {
        val ms = (data[claimName] as? Double)?.toLong() ?: return null
        return Date(ms * 1000)
    }

    private fun getString(data: Map<String, Any>, claimName: String): String? {
        return data[claimName] as? String
    }
}