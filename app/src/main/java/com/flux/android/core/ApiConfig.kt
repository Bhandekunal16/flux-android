package com.flux.android.core

import com.example.BuildConfig

/**
 * Centralized API configuration for the Flux platform.
 * Supports configurable backend URLs via Gradle BuildConfig or runtime overrides.
 */
object ApiConfig {
    // Configurable backend URL injected via BuildConfig.FLUX_BASE_URL
    @Volatile
    var customBaseUrl: String? = null

    val BASE_URL: String
        get() {
            val custom = customBaseUrl?.trim()
            if (!custom.isNullOrEmpty()) {
                return if (custom.endsWith("/")) custom else "$custom/"
            }
            val configUrl = BuildConfig.FLUX_BASE_URL.trim()
            return if (configUrl.isNotEmpty()) {
                if (configUrl.endsWith("/")) configUrl else "$configUrl/"
            } else {
                "https://ais-dev-2zaba6hzkafc5s3m46r33w-880848387352.asia-east1.run.app/"
            }
        }
}
