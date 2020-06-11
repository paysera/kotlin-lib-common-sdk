package com.paysera.lib.common.moshi.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.util.*

class DateAdapter {

    @FromJson
    fun fromJson(date: String): Date {
        val time = date.toLong()
        time.let {
            return Date(it * 1000L)
        }
    }

    @ToJson
    fun toJson(date: Date): String {
        return date.time.div(1000).toString()
    }
}