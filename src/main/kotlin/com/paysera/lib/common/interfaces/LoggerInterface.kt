package com.paysera.lib.common.interfaces

import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor

interface LoggerInterface {
    val level: HttpLoggingInterceptor.Level
    fun log(request: Request, error: Throwable)
}