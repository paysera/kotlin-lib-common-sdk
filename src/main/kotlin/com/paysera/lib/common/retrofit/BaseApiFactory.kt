package com.paysera.lib.common.retrofit

import com.google.gson.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES
import com.google.gson.GsonBuilder
import com.paysera.lib.common.adapters.CoroutineCallAdapterFactory
import com.paysera.lib.common.adapters.RefreshingCoroutineCallAdapterFactory
import com.paysera.lib.common.entities.ApiCredentials
import com.paysera.lib.common.interfaces.BaseApiClient
import com.paysera.lib.common.interfaces.TokenRefresherInterface
import com.paysera.lib.common.serializers.DateSerializer
import com.paysera.lib.common.serializers.MoneySerializer
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.joda.money.Money
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit

abstract class BaseApiFactory<T : BaseApiClient>(private val credentials: ApiCredentials?, private val defaultTimeout: Long = 1200000) {

    abstract fun createClient(baseUrl: String, tokenRefresher: TokenRefresherInterface?): T

    protected fun createRetrofit(baseUrl: String, tokenRefresher: TokenRefresherInterface?): Retrofit {
        return with(Retrofit.Builder()) {
            baseUrl(baseUrl)
            if (tokenRefresher != null && credentials != null) {
                addCallAdapterFactory(RefreshingCoroutineCallAdapterFactory(credentials, tokenRefresher))
            } else {
                addCallAdapterFactory(CoroutineCallAdapterFactory())
            }
            addConverterFactory(createGsonConverterFactory())
            client(createOkHttpClient())
            build()
        }
    }

    protected fun createGsonConverterFactory(): GsonConverterFactory {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.setFieldNamingPolicy(LOWER_CASE_WITH_UNDERSCORES)
        gsonBuilder.registerTypeAdapter(Money::class.java, MoneySerializer())
        gsonBuilder.registerTypeAdapter(Date::class.java, DateSerializer())
        return GsonConverterFactory.create(gsonBuilder.create())
    }

    private fun createOkHttpClient(): OkHttpClient {
        return with(OkHttpClient().newBuilder()) {
            readTimeout(defaultTimeout, TimeUnit.MILLISECONDS)
            writeTimeout(defaultTimeout, TimeUnit.MILLISECONDS)
            connectTimeout(defaultTimeout, TimeUnit.MILLISECONDS)
            credentials?.let {
                addInterceptor { chain ->
                    val originalRequest = chain.request()
                    val builder =
                        originalRequest.newBuilder().header("Authorization", "Bearer ${credentials.token}")
                    val modifiedRequest = builder.build()
                    chain.proceed(modifiedRequest)
                }
            }
            addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
            build()
        }
    }
}