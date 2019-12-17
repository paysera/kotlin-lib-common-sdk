package com.paysera.lib.common.retrofit

import com.google.gson.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES
import com.google.gson.GsonBuilder
import com.paysera.lib.common.adapters.CoroutineCallAdapterFactory
import com.paysera.lib.common.adapters.RefreshingCoroutineCallAdapterFactory
import com.paysera.lib.common.entities.ApiCredentials
import com.paysera.lib.common.extensions.cancellableCallAdapterFactories
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

typealias RetrofitConfiguration = Pair<Retrofit, ApiRequestManager>

abstract class BaseApiFactory<T : BaseApiClient>(
    private val credentials: ApiCredentials?,
    private val timeout: Long? = null
) {
    abstract fun createClient(baseUrl: String, tokenRefresher: TokenRefresherInterface?): T

    protected fun createRetrofit(
        baseUrl: String,
        tokenRefresher: TokenRefresherInterface?
    ): RetrofitConfiguration {
        val okHttpClient = createOkHttpClient()
        val callAdapterFactory = when {
            tokenRefresher != null && credentials != null -> {
                RefreshingCoroutineCallAdapterFactory(credentials, tokenRefresher)
            }
            else -> CoroutineCallAdapterFactory()
        }
        with(Retrofit.Builder()) {
            baseUrl(baseUrl)
            addCallAdapterFactory(callAdapterFactory)
            addConverterFactory(createGsonConverterFactory())
            client(okHttpClient)
            build()
        }.apply {
            return RetrofitConfiguration(
                this,
                ApiRequestManager(okHttpClient, cancellableCallAdapterFactories())
            )
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
            timeout?.let {
                readTimeout(it, TimeUnit.MILLISECONDS)
                writeTimeout(it, TimeUnit.MILLISECONDS)
                connectTimeout(it, TimeUnit.MILLISECONDS)
            }
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