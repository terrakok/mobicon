package com.github.terrakok.mobicon

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.github.terrakok.mobicon.ui.RootContent
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme
import com.materialkolor.rememberDynamicMaterialThemeState

internal val LocalThemeIsDark = compositionLocalOf { mutableStateOf(true) }

@Preview
@Composable
fun App(
    onThemeChanged: @Composable (isDark: Boolean) -> Unit = {}
) = WithAppGraph {
    val systemIsDark = isSystemInDarkTheme()
    val isDarkState = remember(systemIsDark) { mutableStateOf(systemIsDark) }
    CompositionLocalProvider(
        LocalThemeIsDark provides isDarkState
    ) {
        val isDark by isDarkState
        onThemeChanged(!isDark)

        val colorScheme = rememberDynamicMaterialThemeState(
            isDark = isDark,
            style = PaletteStyle.FruitSalad,
            primary = Color(0xFF3713EC),
            secondary = Color(0xFFF97316),
            tertiary = Color(0xFF22C55E),
            error = Color(0xFFEC4899),
        )
        MaterialTheme(
            colorScheme = colorScheme.colorScheme,
            content = { Surface { RootContent() } }
        )
    }
}
