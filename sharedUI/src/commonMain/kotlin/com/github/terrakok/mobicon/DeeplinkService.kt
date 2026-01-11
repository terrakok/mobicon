package com.github.terrakok.mobicon

import androidx.compose.runtime.Immutable
import com.github.terrakok.mobicon.ui.root.AppScreen
import com.github.terrakok.mobicon.ui.root.EventInfoScreen
import com.github.terrakok.mobicon.ui.root.EventScreen
import com.github.terrakok.mobicon.ui.root.EventsListScreen
import com.github.terrakok.mobicon.ui.root.SessionScreen
import com.github.terrakok.mobicon.ui.root.SpeakerScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Immutable
class DeeplinkService {
    private val state = MutableStateFlow("")
    val deepLink: StateFlow<String> get() = state
    fun setDeepLink(link: String) {
        state.value = link
    }

    companion object {
        fun screenToUrl(screen: AppScreen) = when (screen) {
            is EventScreen -> "#/event/${screen.id}"
            is EventInfoScreen -> "#/event/${screen.id}/info"
            is SessionScreen -> "#/event/${screen.eventId}?session=${screen.id}"
            is SpeakerScreen -> "#/event/${screen.eventId}?speaker=${screen.id}"
            else -> null
        }

        fun urlToStack(url: String): List<AppScreen> {
            val initLocation = url.substringAfter("#/event/", "")
            when {
                initLocation.isBlank() -> {
                    return listOf(EventsListScreen)
                }

                initLocation.contains("?session=") -> {
                    val (eventId, sessionId) = initLocation.split("?session=")
                    return listOf(EventScreen(eventId), SessionScreen(eventId, sessionId))
                }

                initLocation.contains("/info") -> {
                    val eventId = initLocation.substringBefore("/info")
                    return listOf(EventScreen(eventId), EventInfoScreen(eventId))
                }

                initLocation.contains("?speaker=") -> {
                    val (eventId, speakerId) = initLocation.split("?speaker=")
                    return listOf(EventScreen(eventId), SpeakerScreen(eventId, speakerId))
                }

                else -> {
                    val eventId = initLocation
                    return listOf(EventScreen(eventId))
                }
            }
        }
    }
}