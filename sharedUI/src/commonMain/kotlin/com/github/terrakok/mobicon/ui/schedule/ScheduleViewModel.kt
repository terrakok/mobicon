package com.github.terrakok.mobicon.ui.schedule

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.terrakok.mobicon.DataService
import com.github.terrakok.mobicon.EventFullData
import com.github.terrakok.mobicon.EventInfo
import dev.zacsweers.metro.*
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.coroutines.launch

@AssistedInject
internal class ScheduleViewModel(
    @Assisted val eventId: String,
    private val dataService: DataService
) : ViewModel() {
    @AssistedFactory
    @ManualViewModelAssistedFactoryKey(Factory::class)
    @ContributesIntoMap(AppScope::class)
    interface Factory : ManualViewModelAssistedFactory {
        fun create(eventId: String): ScheduleViewModel
    }

    var eventFullData by mutableStateOf<EventFullData?>(null)
        private set

    var eventInfo by mutableStateOf<EventInfo?>(null)
        private set

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                eventInfo = dataService.getEventInfo(eventId)
                eventFullData = dataService.getEventFullData(eventId)
            } catch (e: Throwable) {
            }
        }
    }
}