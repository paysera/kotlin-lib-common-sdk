package com.paysera.lib.common.adapters

import kotlinx.coroutines.CompletableDeferred
import retrofit2.Call

data class CallAdapterRequest(
    val call: Call<Any>,
    val deferred: CompletableDeferred<*>,
    val isResponse: Boolean = false
) {
    fun clone(): CallAdapterRequest {
        return CallAdapterRequest(call.clone(), deferred, isResponse)
    }
}