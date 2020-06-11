package com.paysera.lib.common.moshi.adapters

import com.paysera.lib.common.entities.MetadataAwareResponse
import com.paysera.lib.common.entities.Metadata
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class MetadataAwareResponseAdapter<T : Any>(
    private val moshi: Moshi,
    private val adapter: JsonAdapter<T>
) : JsonAdapter<MetadataAwareResponse<T>>() {

    object Factory: JsonAdapter.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? {
            val paramType = type as? ParameterizedType ?: return null
            if (paramType.rawType != MetadataAwareResponse::class.java) return null
            if (paramType.actualTypeArguments.size != 1) return null
            val argType = paramType.actualTypeArguments[0]
            val adapter = moshi.adapter<Any>(argType)

            return MetadataAwareResponseAdapter(moshi, adapter)
        }
    }

    override fun fromJson(reader: JsonReader): MetadataAwareResponse<T> {
        val items = arrayListOf<T>()
        var metadata: Metadata? = null

        reader.beginObject()
        while (reader.hasNext()) {
            val name = reader.nextName()
            if (name == "_metadata") {
                metadata = moshi.adapter(Metadata::class.java).fromJson(reader)!!
            } else {
                reader.beginArray()
                while (reader.hasNext()) {
                    items.add(adapter.fromJson(reader)!!)
                }
                reader.endArray()
            }
        }
        reader.endObject()

        return MetadataAwareResponse(items, metadata!!)
    }

    override fun toJson(writer: JsonWriter, value: MetadataAwareResponse<T>?) {}
}