package com.paysera.lib.common.retrofit

import com.paysera.lib.common.adapters.CoroutineCallAdapterFactory
import com.paysera.lib.common.adapters.RefreshingCoroutineCallAdapterFactory
import com.paysera.lib.common.entities.ApiCredentials
import com.paysera.lib.common.extensions.cancellableCallAdapterFactories
import com.paysera.lib.common.interfaces.TokenRefresherInterface
import com.paysera.lib.common.moshi.adapters.BigDecimalAdapter
import com.paysera.lib.common.moshi.adapters.DateAdapter
import com.paysera.lib.common.moshi.adapters.MetadataAwareResponseAdapter
import com.paysera.lib.common.moshi.adapters.MoneyAdapter
import com.squareup.moshi.Moshi
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
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
            addConverterFactory(createMoshiConverterFactory())
            client(okHttpClient)
            build()
        }.apply {
            return RetrofitConfiguration(
                this,
                ApiRequestManager(okHttpClient, cancellableCallAdapterFactories())
            )
        }
    }

    protected open fun createMoshiConverterFactory(): MoshiConverterFactory {
        val moshiBuilder = Moshi.Builder()
            .add(BigDecimalAdapter())
            .add(DateAdapter())
            .add(MoneyAdapter())
            .add(MetadataAwareResponseAdapter.Factory)
        return MoshiConverterFactory.create(moshiBuilder.build())
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