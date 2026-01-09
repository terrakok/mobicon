package com.github.terrakok.mobicon.ui

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.github.terrakok.navigation3.browser.HierarchicalBrowserNavigation
import kotlinx.browser.window

@Composable
internal actual fun BrowserNavigation(backStack: NavBackStack<NavKey>) {
    HierarchicalBrowserNavigation {
        when (val current = backStack.last() as AppScreen) {
            is EventScreen -> "#/event/${current.id}"
            is SessionScreen -> "#/event/${current.eventId}?session=${current.id}"
            is SpeakerScreen -> "#/event/${current.eventId}?speaker=${current.id}"
            else -> ""
        }
    }
}

internal actual fun getInitialBackStack(): List<NavKey> {
    val initLocation = window.location.toString()
        .substringAfter("#/event/", "")
    when {
        initLocation.isBlank() -> {
            return listOf(EventsListScreen)
        }
        initLocation.contains("?session=") -> {
            val (eventId, sessionId) = initLocation.split("?session=")
            return listOf(EventScreen(eventId), SessionScreen(eventId, sessionId))
        }
        initLocation.contains("?speaker=") -> {
            val (eventId, speakerId) = initLocation.split("?speaker=")
            return listOf(EventScreen(eventId), SpeakerScreen(eventId, speakerId))
        }
        else -> {
            return listOf(EventScreen(initLocation))
        }
    }
}