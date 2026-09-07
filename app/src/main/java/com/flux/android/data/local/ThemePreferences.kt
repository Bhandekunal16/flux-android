package com.flux.android.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

enum class ThemeMode {
    SYSTEM,
    DARK,
    LIGHT,
}

enum class AccentTheme {
    EDITORIAL, // Lilac #D0BCFF, Mauve #CCC2DC, Violet #381E72 (Editorial Aesthetic)
    SUNSET, // Neon orange, flame coral, violet
    OCEAN, // Cyan, electric blue, sapphire
    FOREST, // Emerald, mint, deep teal
    BERRY, // Magenta, deep rose, violet
}

data class ThemeConfig(
    val mode: ThemeMode = ThemeMode.DARK,
    val accent: AccentTheme = AccentTheme.EDITORIAL,
)

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "flux_theme_prefs")

class ThemePreferences(
    private val context: Context,
) {
    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ACCENT_THEME = stringPreferencesKey("accent_theme")
    }

    val themeConfig: Flow<ThemeConfig> =
        context.themeDataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }.map { prefs ->
                val modeStr = prefs[Keys.THEME_MODE] ?: ThemeMode.DARK.name
                val accentStr = prefs[Keys.ACCENT_THEME] ?: AccentTheme.EDITORIAL.name
                val mode = runCatching { ThemeMode.valueOf(modeStr) }.getOrDefault(ThemeMode.DARK)
                val accent = runCatching { AccentTheme.valueOf(accentStr) }.getOrDefault(AccentTheme.EDITORIAL)
                ThemeConfig(mode = mode, accent = accent)
            }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.themeDataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.name
        }
    }

    suspend fun setAccentTheme(accent: AccentTheme) {
        context.themeDataStore.edit { prefs ->
            prefs[Keys.ACCENT_THEME] = accent.name
        }
    }
}
