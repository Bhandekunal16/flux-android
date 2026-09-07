package com.flux.android.presentation.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flux.android.data.local.AccentTheme
import com.flux.android.data.local.ThemeConfig
import com.flux.android.data.local.ThemeMode
import com.flux.android.data.local.ThemePreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeViewModel(
    private val themePreferences: ThemePreferences
) : ViewModel() {

    val themeConfig: StateFlow<ThemeConfig> = themePreferences.themeConfig
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeConfig()
        )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            themePreferences.setThemeMode(mode)
        }
    }

    fun setAccentTheme(accent: AccentTheme) {
        viewModelScope.launch {
            themePreferences.setAccentTheme(accent)
        }
    }
}
