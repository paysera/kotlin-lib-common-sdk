package com.paysera.lib.common.retrofit

import retrofit2.Retrofit

data class RetrofitConfiguration(
    val retrofit: Retrofit,
    val apiRequestManager: ApiRequestManager
)