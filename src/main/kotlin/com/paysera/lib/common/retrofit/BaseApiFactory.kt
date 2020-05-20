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
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.joda.money.Money
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit


abstract class BaseApiFactory<T : BaseApiClient>(
    private val userAgent: String?,
    private val credentials: ApiCredentials?,
    private val timeout: Long? = null,
    private val httpLoggingInterceptorLevel: HttpLoggingInterceptor.Level = HttpLoggingInterceptor.Level.BASIC
) {
    abstract val baseUrl: String
    abstract val certifiedHosts: List<String>

    abstract fun createClient(tokenRefresher: TokenRefresherInterface?): T

    protected fun createRetrofit(tokenRefresher: TokenRefresherInterface?): RetrofitConfiguration {
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

    protected open fun createGsonConverterFactory(): GsonConverterFactory {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.setFieldNamingPolicy(LOWER_CASE_WITH_UNDERSCORES)
        gsonBuilder.registerTypeAdapter(Money::class.java, MoneySerializer())
        gsonBuilder.registerTypeAdapter(Date::class.java, DateSerializer())
        return GsonConverterFactory.create(gsonBuilder.create())
    }

    private fun createOkHttpClient(): OkHttpClient {
        val certificatePinnerBuilder = CertificatePinner.Builder().apply {
            certifiedHosts.forEach {
                add(it, "sha256/K8WscGYwD51wz79WudzZPDSXFRYrKM+e78Y5YQZJG3k=")
                add(it, "sha256/9ay/M3fmRBbc/7R5Nqts0SuDQK8KjAHUSZlLCxEPsH0=")
            }
        }
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
                    userAgent?.let {
                        builder.header("User-Agent", it)
                    }
                    credentials?.let {
                        builder.header("Authorization", "Bearer ${it.token}")
                    }
                    val modifiedRequest = builder.build()
                    chain.proceed(modifiedRequest)
                }
            }
            addInterceptor(HttpLoggingInterceptor().setLevel(httpLoggingInterceptorLevel))
            retryOnConnectionFailure(false)
            certificatePinner(certificatePinnerBuilder.build())
            build()
        }
    }
}