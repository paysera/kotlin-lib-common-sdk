package com.paysera.lib.common.retrofit

import com.google.gson.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES
import com.google.gson.GsonBuilder
import com.paysera.lib.common.adapters.CoroutineCallAdapterFactory
import com.paysera.lib.common.adapters.RefreshingCoroutineCallAdapterFactory
import com.paysera.lib.common.entities.PayseraApiCredentials
import com.paysera.lib.common.entities.CustomApiCredentials
import com.paysera.lib.common.entities.InRentoApiCredentials
import com.paysera.lib.common.extensions.cancellableCallAdapterFactories
import com.paysera.lib.common.interfaces.BaseApiCredentials
import com.paysera.lib.common.interfaces.ErrorLoggerInterface
import com.paysera.lib.common.interfaces.TokenRefresherInterface
import com.paysera.lib.common.serializers.DateSerializer
import com.paysera.lib.common.serializers.MoneySerializer
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.joda.money.Money
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit

abstract class BaseApiFactory<T : BaseApiClient>(
    private val baseUrl: String,
    private val userAgent: String?,
    private val credentials: BaseApiCredentials?,
    private val timeout: Long? = null,
    private val httpLoggingInterceptorLevel: HttpLoggingInterceptor.Level = HttpLoggingInterceptor.Level.BASIC,
    private val errorLogger: ErrorLoggerInterface,
    private val certificateInterceptor: Interceptor? = null
) {
    abstract fun createClient(tokenRefresher: TokenRefresherInterface?): T

    protected fun createRetrofit(tokenRefresher: TokenRefresherInterface?): RetrofitConfiguration {
        val okHttpClient = createOkHttpClient()
        val callAdapterFactory = when {
            tokenRefresher != null && credentials != null -> {
                RefreshingCoroutineCallAdapterFactory(credentials, tokenRefresher, errorLogger)
            }
            else -> CoroutineCallAdapterFactory(errorLogger)
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

    protected open fun createGsonConverterFactory(): GsonConverterFactory {
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
            if (credentials != null || userAgent != null) {
                addInterceptor { chain ->
                    val originalRequest = chain.request()
                    val builder = originalRequest.newBuilder()
                    (credentials as? PayseraApiCredentials)?.let {
                        builder.header("Accept-Language", it.locale)
                        userAgent?.let {
                            builder.header("User-Agent", it)
                        }
                        builder.header("Authorization", "Bearer ${it.token}")
                    }
                    (credentials as? InRentoApiCredentials)?.let {
                        builder.header("x-auth-token", it.token ?: "")
                        builder.header("x-locale", it.locale)
                    }
                    (credentials as? CustomApiCredentials)?.let {
                        builder.header(it.key ?: "", it.token ?: "")
                    }
                    val modifiedRequest = builder.build()
                    chain.proceed(modifiedRequest)
                }
            }
            addInterceptor(HttpLoggingInterceptor().setLevel(httpLoggingInterceptorLevel))
            certificateInterceptor?.let { addNetworkInterceptor(it) }
            retryOnConnectionFailure(false)
            build()
        }
    }
}