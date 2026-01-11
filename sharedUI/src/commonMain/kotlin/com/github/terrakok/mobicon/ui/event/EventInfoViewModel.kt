package com.github.terrakok.mobicon.ui.event

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.terrakok.mobicon.DataService
import com.github.terrakok.mobicon.EventInfo
import dev.zacsweers.metro.*
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.coroutines.launch

@AssistedInject
internal class EventInfoViewModel(
    @Assisted("eventId") val eventId: String,
    private val dataService: DataService
) : ViewModel() {
    @AssistedFactory
    @ManualViewModelAssistedFactoryKey(Factory::class)
    @ContributesIntoMap(AppScope::class)
    interface Factory : ManualViewModelAssistedFactory {
        fun create(
            @Assisted("eventId") eventId: String,
        ): EventInfoViewModel
    }

    var eventInfo by mutableStateOf<EventInfo?>(null)
        private set

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            eventInfo = dataService.getEventInfo(eventId)
        }
    }
}