package com.github.terrakok.mobicon.ui.session

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.terrakok.mobicon.CategoryItem
import com.github.terrakok.mobicon.DataService
import com.github.terrakok.mobicon.EventInfo
import com.github.terrakok.mobicon.Room
import com.github.terrakok.mobicon.Session
import com.github.terrakok.mobicon.Speaker
import dev.zacsweers.metro.*
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.coroutines.launch

@AssistedInject
internal class SessionViewModel(
    @Assisted("eventId") val eventId: String,
    @Assisted("sessionId") val sessionId: String,
    private val dataService: DataService
) : ViewModel() {

    @AssistedFactory
    @ManualViewModelAssistedFactoryKey(Factory::class)
    @ContributesIntoMap(AppScope::class)
    interface Factory : ManualViewModelAssistedFactory {
        fun create(
            @Assisted("eventId") eventId: String,
            @Assisted("sessionId") sessionId: String
        ): SessionViewModel
    }

    var event by mutableStateOf<EventInfo?>(null)
        private set
    var session by mutableStateOf<Session?>(null)
        private set
    var room by mutableStateOf<Room?>(null)
        private set
    private val speakersState = mutableStateListOf<Speaker>()
    val speakers: List<Speaker> = speakersState
    private val categoryItemsState = mutableStateListOf<CategoryItem>()
    val categoryItems: List<CategoryItem> = categoryItemsState

    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    init {
        loadSession()
    }

    fun loadSession() {
        viewModelScope.launch {
            try {
                loading = true
                error = null
                event = dataService.getEventInfo(eventId)
                val s = dataService.getSession(eventId, sessionId)
                session = s
                if (s.roomId != null) {
                    room = dataService.getRoom(eventId, s.roomId)
                }
                speakersState.clear()
                speakersState.addAll(s.speakers.map { dataService.getSpeaker(eventId, it) })
                categoryItemsState.clear()
                categoryItemsState.addAll(s.categoryItems.map { dataService.getCategoryItem(eventId, it) })
            } catch (e: Throwable) {
                error = e.message
            } finally {
                loading = false
            }
        }
    }
}