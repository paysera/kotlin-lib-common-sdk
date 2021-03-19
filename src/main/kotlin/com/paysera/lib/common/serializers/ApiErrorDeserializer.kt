package com.paysera.lib.common.serializers

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import com.paysera.lib.common.exceptions.ApiError
import com.paysera.lib.common.exceptions.ApiErrorField
import com.paysera.lib.common.exceptions.ApiErrorProperty
import java.lang.reflect.Type

class ApiErrorDeserializer : JsonDeserializer<ApiError> {

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): ApiError {
        val jsonObject = json?.asJsonObject
        return ApiError().also {
            it.error = jsonObject?.get("error")?.asString
            it.description = jsonObject?.get("error_description")?.asString
            it.statusCode = jsonObject?.get("statusCode")?.asInt
            it.properties = getErrorProperties(jsonObject?.get("error_properties"))
            it.errorFields = jsonObject?.get("errors")
                ?.asJsonArray
                ?.map {
                    ApiErrorField(
                        code = it.asJsonObject.get("code")?.asString ?: "",
                        field = it.asJsonObject.get("field")?.asString ?: "",
                        message = it.asJsonObject.get("message")?.asString ?: "",
                    )
                }
            it.data = context?.deserialize<Any>(jsonObject?.get("error_data"), TypeToken.get(Any::class.java).type)
        }
    }

    private fun getErrorProperties(jsonElement: JsonElement?): List<ApiErrorProperty>? {
        jsonElement ?: return null
        if (jsonElement.isJsonArray) {
            val jsonArray = jsonElement.asJsonArray
            if (jsonArray.size() == 0) return null
            return jsonArray.map {
                it.asJsonObject
            }.map {
                ApiErrorProperty(
                    code = it.get("code").asString,
                    description = it.get("description").asString
                )
            }
        } else if (jsonElement.isJsonObject) {
            jsonElement.asJsonObject
                ?.entrySet()
                ?.firstOrNull()
                ?.let {
                    return listOf(ApiErrorProperty(it.key, it.value.asString))
                }
        }
        return null
    }
}