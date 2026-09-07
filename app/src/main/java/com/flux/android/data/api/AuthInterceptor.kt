package com.flux.android.data.api

import com.flux.android.data.local.AuthPreferences
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val authPreferences: AuthPreferences,
    private val onUnauthorized: () -> Unit = {},
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = authPreferences.currentToken

        val requestBuilder =
            original
                .newBuilder()
                .header("Accept", "application/json")

        if (!token.isNullOrEmpty()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        val response = chain.proceed(requestBuilder.build())

        if (response.code == 401) {
            onUnauthorized()
        }

        return response
    }
}
