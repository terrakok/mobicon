package com.github.terrakok.mobicon.ui

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

@Composable
internal actual fun BrowserNavigation(backStack: NavBackStack<NavKey>) = Unit
internal actual fun getInitialBackStack(): List<NavKey> = listOf(EventsListScreen)