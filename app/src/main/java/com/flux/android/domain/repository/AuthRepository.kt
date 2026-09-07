package com.flux.android.domain.repository

import com.flux.android.core.Resource
import com.flux.android.domain.model.UserSession
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String): Resource<UserSession>

    suspend fun logout()

    fun getSession(): Flow<UserSession?>

    fun getCurrentToken(): String?

    fun isLoggedIn(): Boolean
}
