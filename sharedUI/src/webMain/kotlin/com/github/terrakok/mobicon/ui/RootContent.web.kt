package com.github.terrakok.mobicon.ui

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.github.terrakok.navigation3.browser.HierarchicalBrowserNavigation

@Composable
internal actual fun BrowserNavigation(backStack: NavBackStack<NavKey>) {
    HierarchicalBrowserNavigation {
        when (val current = backStack.last() as AppScreen) {
            is Event -> "#event?id=${current.info.id}"
            else -> ""
        }
    }
}