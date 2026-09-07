package com.flux.android.di

import android.content.Context
import com.flux.android.core.ApiConfig
import com.flux.android.core.FluxAnalytics
import com.flux.android.data.api.AuthInterceptor
import com.flux.android.data.api.FluxApi
import com.flux.android.data.local.AuthPreferences
import com.flux.android.data.local.ThemePreferences
import com.flux.android.data.player.FluxPlayerManager
import com.flux.android.data.repository.AuthRepositoryImpl
import com.flux.android.data.repository.FluxRepositoryImpl
import com.flux.android.domain.repository.AuthRepository
import com.flux.android.domain.repository.FluxRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class AppContainer(
    val context: Context,
) {
    val analytics: FluxAnalytics by lazy {
        FluxAnalytics()
    }

    val authPreferences: AuthPreferences by lazy {
        AuthPreferences(context)
    }

    val themePreferences: ThemePreferences by lazy {
        ThemePreferences(context)
    }

    val moshi: Moshi by lazy {
        Moshi
            .Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    private val authInterceptor: AuthInterceptor by lazy {
        AuthInterceptor(authPreferences)
    }

    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.NONE
        }
    }

    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient
            .Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .retryOnConnectionFailure(true)
            .build()
    }

    private var currentBaseUrl: String = ApiConfig.BASE_URL
    private var internalRetrofit: Retrofit = buildRetrofit(currentBaseUrl)
    private var internalFluxApi: FluxApi = internalRetrofit.create(FluxApi::class.java)

    private fun buildRetrofit(baseUrl: String): Retrofit {
        val safeUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return Retrofit
            .Builder()
            .baseUrl(safeUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    val fluxApi: FluxApi
        get() {
            val configured = ApiConfig.BASE_URL
            if (configured != currentBaseUrl) {
                currentBaseUrl = configured
                internalRetrofit = buildRetrofit(configured)
                internalFluxApi = internalRetrofit.create(FluxApi::class.java)
            }
            return internalFluxApi
        }

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(fluxApi, authPreferences, analytics)
    }

    val fluxRepository: FluxRepository by lazy {
        FluxRepositoryImpl(fluxApi, moshi, analytics)
    }

    val playerManager: FluxPlayerManager by lazy {
        FluxPlayerManager(context, fluxRepository, analytics)
    }
}
