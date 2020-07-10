package com.paysera.lib.common.interfaces

import com.paysera.lib.common.exceptions.ApiError
import okhttp3.Request

interface ErrorLoggerInterface {
    fun log(request: Request, error: ApiError)
}