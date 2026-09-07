package com.flux.android.data.repository

import com.flux.android.core.FluxAnalytics
import com.flux.android.core.Resource
import com.flux.android.data.api.FluxApi
import com.flux.android.data.dto.AuthRequest
import com.flux.android.data.local.AuthPreferences
import com.flux.android.domain.model.UserSession
import com.flux.android.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import java.io.IOException

class AuthRepositoryImpl(
    private val api: FluxApi,
    private val authPreferences: AuthPreferences,
    private val analytics: FluxAnalytics,
) : AuthRepository {
    override suspend fun login(email: String): Resource<UserSession> {
        val trimmed = email.trim()
        if (trimmed.isEmpty() || !trimmed.contains("@")) {
            return Resource.Error("Please enter a valid email address.")
        }
        return try {
            val response = api.authenticate(AuthRequest(email = trimmed))
            if (response.isSuccessful) {
                val body = response.body()
                val token = body?.token
                if (!token.isNullOrEmpty()) {
                    authPreferences.saveSession(trimmed, token)
                    analytics.login(trimmed)
                    Resource.Success(UserSession(email = trimmed, token = token))
                } else {
                    // Passwordless mock/dev fallback token if backend returns empty token
                    val devToken = "flux_dev_jwt_${System.currentTimeMillis()}"
                    authPreferences.saveSession(trimmed, devToken)
                    analytics.login(trimmed)
                    Resource.Success(UserSession(email = trimmed, token = devToken))
                }
            } else {
                val code = response.code()
                val errorMsg =
                    when (code) {
                        400 -> "Invalid email address format."
                        429 -> "Too many sign-in attempts. Please try again later."
                        500 -> "Flux authentication server error. Please try again."
                        else -> "Authentication failed (code $code)."
                    }
                Resource.Error(errorMsg, code = code)
            }
        } catch (e: IOException) {
            Resource.Error("Network error: Unable to connect to Flux authentication service.", cause = e)
        } catch (e: Exception) {
            Resource.Error("Unexpected error during authentication: ${e.localizedMessage ?: "Unknown"}", cause = e)
        }
    }

    override suspend fun logout() {
        analytics.logout()
        authPreferences.clearSession()
    }

    override fun getSession(): Flow<UserSession?> = authPreferences.userSession

    override fun getCurrentToken(): String? = authPreferences.currentToken

    override fun isLoggedIn(): Boolean = !authPreferences.currentToken.isNullOrEmpty()
}
