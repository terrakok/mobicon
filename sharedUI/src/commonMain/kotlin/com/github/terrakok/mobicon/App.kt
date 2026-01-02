package com.github.terrakok.mobicon

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.github.terrakok.mobicon.ui.RootContent
import com.github.terrakok.mobicon.ui.events.EventsListScreen
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme

internal val LocalThemeIsDark = compositionLocalOf { mutableStateOf(true) }

internal object Colors {
    val Blue = Color(0xFF61C7EA)
    val Purple = Color(0xFFC2BCF5)
    val Green = Color(0xFF71C09D)
    val Yellow = Color(0xFFF3BB54)
    val Orange = Color(0xFFF8643A)

    fun getForString(str: String): Color {
        val index = str.map { it.code }.sum() % 5
        return when (index) {
            0 -> Blue
            1 -> Purple
            2 -> Green
            3 -> Yellow
            else -> Orange
        }
    }
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

        val colorScheme = rememberDynamicColorScheme(
            seedColor = Color(0xFF61C7EA),
            isDark = isDark,
            specVersion = ColorSpec.SpecVersion.SPEC_2025,
            style = PaletteStyle.Neutral
        )
        MaterialTheme(
            colorScheme = colorScheme,
            content = { Surface { RootContent() } }
        )
    }
}
