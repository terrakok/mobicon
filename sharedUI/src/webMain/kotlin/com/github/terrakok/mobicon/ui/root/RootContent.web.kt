package com.github.terrakok.mobicon.ui.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.github.terrakok.mobicon.DeeplinkService
import com.github.terrakok.navigation3.browser.HierarchicalBrowserNavigation

@Composable
internal actual fun BrowserNavigation(backStack: NavBackStack<NavKey>) {
    HierarchicalBrowserNavigation(
        currentDestination = remember { derivedStateOf { backStack.lastOrNull() } },
        currentDestinationName = { DeeplinkService.screenToUrl(it as AppScreen) }
    )
}