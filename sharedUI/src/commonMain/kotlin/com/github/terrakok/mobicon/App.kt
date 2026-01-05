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

internal object Colors {
    val Primary = Color(0xff0496ff)
    val Secondary = Color(0xFFffbc42)
    val Tertiary = Color(0xFF8f2d56)
    val Error = Color(0xFFd81159)
    val Neutral = Color(0xFFEEEEEE)
    val NeutralVariant = Color(0xFF444444)
}

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
            primary = Colors.Primary,
            secondary = Colors.Secondary,
            tertiary = Colors.Tertiary,
            error = Colors.Error,
            neutral = Colors.Neutral,
            neutralVariant = Colors.NeutralVariant,
        )
        MaterialTheme(
            colorScheme = colorScheme.colorScheme,
            content = { Surface { RootContent() } }
        )
    }
}
