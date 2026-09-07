package com.flux.android.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.flux.android.domain.model.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "flux_auth_prefs")

class AuthPreferences(
    private val context: Context,
) {
    private object PreferencesKeys {
        val TOKEN = stringPreferencesKey("auth_token")
        val EMAIL = stringPreferencesKey("auth_email")
    }

    val userSession: Flow<UserSession?> =
        context.authDataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }.map { preferences ->
                val token = preferences[PreferencesKeys.TOKEN]
                val email = preferences[PreferencesKeys.EMAIL]
                if (!token.isNullOrEmpty() && !email.isNullOrEmpty()) {
                    UserSession(email = email, token = token)
                } else {
                    null
                }
            }

    // In-memory cache of current token for synchronous OkHttp interceptor access
    @Volatile
    var currentToken: String? = null
        private set

    @Volatile
    var currentEmail: String? = null
        private set

    suspend fun saveSession(
        email: String,
        token: String,
    ) {
        currentToken = token
        currentEmail = email
        context.authDataStore.edit { preferences ->
            preferences[PreferencesKeys.TOKEN] = token
            preferences[PreferencesKeys.EMAIL] = email
        }
    }

    suspend fun clearSession() {
        currentToken = null
        currentEmail = null
        context.authDataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.TOKEN)
            preferences.remove(PreferencesKeys.EMAIL)
        }
    }
}
