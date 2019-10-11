package com.paysera.lib.common.jwt

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException

import java.lang.reflect.Type
import java.util.ArrayList
import java.util.Date
import java.util.HashMap

internal class JWTDeserializer : JsonDeserializer<JWTPayload> {

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): JWTPayload {
        if (json.isJsonNull || !json.isJsonObject) {
            throw DecodeException("The token's payload had an invalid JSON format.")
        }

        val jsonObject = json.asJsonObject

        //Public Claims
        val iss = getString(jsonObject, "iss")
        val sub = getString(jsonObject, "sub")
        val exp = getDate(jsonObject, "exp")
        val nbf = getDate(jsonObject, "nbf")
        val iat = getDate(jsonObject, "iat")
        val jti = getString(jsonObject, "jti")
        val aud = getStringOrArray(jsonObject, "aud")

        //Private Claims
        val extra = HashMap<String, Claim>()
        for ((key, value) in jsonObject.entrySet()) {
            extra[key] = ClaimImpl(value)
        }

        return JWTPayload(iss, sub, exp, nbf, iat, jti, aud, extra)
    }

    private fun getStringOrArray(obj: JsonObject, claimName: String): List<String> {
        var list = mutableListOf<String>()
        if (obj.has(claimName)) {
            val arrElement = obj.get(claimName)
            if (arrElement.isJsonArray) {
                val jsonArr = arrElement.asJsonArray
                list = mutableListOf()
                for (i in 0 until jsonArr.size()) {
                    list.add(jsonArr.get(i).asString)
                }
            } else {
                list = mutableListOf(arrElement.asString)
            }
        }
        return list
    }

    private fun getDate(obj: JsonObject, claimName: String): Date? {
        if (!obj.has(claimName)) {
            return null
        }
        val ms = obj.get(claimName).asLong * 1000
        return Date(ms)
    }

    private fun getString(obj: JsonObject, claimName: String): String? {
        return if (!obj.has(claimName)) {
            null
        } else obj.get(claimName).asString
    }
}