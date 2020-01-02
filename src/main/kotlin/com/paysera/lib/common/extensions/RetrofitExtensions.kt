package com.paysera.lib.common.extensions

import com.paysera.lib.common.interfaces.CancellableAdapterFactory
import retrofit2.Retrofit

fun Retrofit.cancellableCallAdapterFactories(): List<CancellableAdapterFactory> {
    return callAdapterFactories().filterIsInstance<CancellableAdapterFactory>()
}