package com.github.terrakok.mobicon.ui.root

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.terrakok.mobicon.ui.DeeplinkService
import com.russhwolf.settings.Settings
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.launch

@ContributesIntoMap(AppScope::class)
@ViewModelKey(RootViewModel::class)
@Inject
internal class RootViewModel(
    private val deeplinkService: DeeplinkService,
    private val settings: Settings
) : ViewModel() {
    private companion object {
        const val LAST_EVENT_ID_KEY = "lastEventId"
    }

    var initialStack = mutableStateListOf<AppScreen>()
        private set

    init {
        viewModelScope.launch {
            deeplinkService.deepLink.collect { url ->
                initialStack.clear()
                initialStack.addAll(urlToStack(url))
            }
        }
    }

    fun selectEvent(eventId: String) {
        settings.putString(LAST_EVENT_ID_KEY, eventId)
    }

    private fun urlToStack(url: String): List<AppScreen> {
        val initLocation = url.substringAfter("#/event/", "")
        when {
            initLocation.isBlank() -> {
                val lastEventId = settings.getStringOrNull(LAST_EVENT_ID_KEY)
                return if (lastEventId != null) {
                    listOf(EventScreen(lastEventId))
                } else {
                    listOf(EventsListScreen)
                }
            }
            initLocation.contains("?session=") -> {
                val (eventId, sessionId) = initLocation.split("?session=")
                selectEvent(eventId)
                return listOf(EventScreen(eventId), SessionScreen(eventId, sessionId))
            }
            initLocation.contains("?speaker=") -> {
                val (eventId, speakerId) = initLocation.split("?speaker=")
                selectEvent(eventId)
                return listOf(EventScreen(eventId), SpeakerScreen(eventId, speakerId))
            }
            else -> {
                val eventId = initLocation
                selectEvent(eventId)
                return listOf(EventScreen(eventId))
            }
        }
    }
}
