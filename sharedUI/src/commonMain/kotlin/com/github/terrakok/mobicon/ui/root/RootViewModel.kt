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

    fun saveSelectedEvent(eventId: String) {
        settings.putString(LAST_EVENT_ID_KEY, eventId)
    }

    private fun urlToStack(url: String): List<AppScreen> {
        val stack = DeeplinkService.urlToStack(url)
        if (stack.size == 1 && stack.single() is EventsListScreen) {
            val lastEventId = settings.getStringOrNull(LAST_EVENT_ID_KEY)
            return if (lastEventId != null) listOf(EventScreen(lastEventId)) else stack
        } else {
            val eventScreen = stack.firstOrNull { it is EventScreen } as EventScreen?
            if (eventScreen != null) {
                saveSelectedEvent(eventScreen.id)
            }
            return stack
        }
    }
}
