package com.paysera.lib.common.retrofit

import com.paysera.lib.common.interfaces.CancellableAdapterFactory
import okhttp3.OkHttpClient

class ApiRequestManager(
    private var okHttpClient: OkHttpClient,
    private val requestAdapters: List<CancellableAdapterFactory>
) {
    fun cancelCalls() {
        requestAdapters.forEach {
            it.cancelCalls()
        }
        okHttpClient.dispatcher.cancelAll()
    }
}