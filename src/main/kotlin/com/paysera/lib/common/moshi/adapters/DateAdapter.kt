package com.paysera.lib.common.moshi.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import java.util.*

class DateAdapter {

    @ToJson
    fun toJson(writer: JsonWriter, date: Date?) {
        if (date != null) {
            writer.value(date.time.div(1000).toString())
        } else {
            writer.nullValue()
        }
    }

    @FromJson
    fun fromJson(date: String): Date {
        val time = date.toLong()
        time.let {
            return Date(it * 1000L)
        }
    }
}