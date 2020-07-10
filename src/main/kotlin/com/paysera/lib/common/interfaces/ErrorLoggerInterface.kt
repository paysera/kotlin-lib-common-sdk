package com.paysera.lib.common.interfaces

import okhttp3.Request

interface ErrorLoggerInterface {
    fun log(request: Request, error: Throwable)
}