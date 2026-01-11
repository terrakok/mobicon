package com.github.terrakok.mobicon.ui.speaker

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.terrakok.mobicon.DataService
import com.github.terrakok.mobicon.Session
import com.github.terrakok.mobicon.Speaker
import dev.zacsweers.metro.*
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.coroutines.launch

@AssistedInject
internal class SpeakerViewModel(
    @Assisted("eventId") val eventId: String,
    @Assisted("speakerId") val speakerId: String,
    private val dataService: DataService
) : ViewModel() {
    @AssistedFactory
    @ManualViewModelAssistedFactoryKey(Factory::class)
    @ContributesIntoMap(AppScope::class)
    interface Factory : ManualViewModelAssistedFactory {
        fun create(
            @Assisted("eventId") eventId: String,
            @Assisted("speakerId") speakerId: String
        ): SpeakerViewModel
    }

    var speaker by mutableStateOf<Speaker?>(null)
        private set

    private val sessiosState = mutableStateListOf<Session>()
    val sessions: List<Session> = sessiosState

    init {
        loadSpeaker()
    }

    private fun loadSpeaker() {
        viewModelScope.launch {
            try {
                val s = dataService.getSpeaker(eventId, speakerId)
                speaker = s
                sessiosState.addAll(
                    s.sessions
                        .map { dataService.getSession(eventId, it.toString()) }
                        .sortedBy { it.startsAt }
                )
            } catch (e: Throwable) {
            }
        }
    }

}