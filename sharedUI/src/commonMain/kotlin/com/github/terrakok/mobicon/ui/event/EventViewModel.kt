package com.github.terrakok.mobicon.ui.event

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.terrakok.mobicon.ApiService
import com.github.terrakok.mobicon.EventFullData
import com.github.terrakok.mobicon.EventInfo
import dev.zacsweers.metro.*
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@AssistedInject
internal class EventViewModel(
    @Assisted val eventInfo: EventInfo,
    private val apiService: ApiService,
) : ViewModel() {
    @AssistedFactory
    @ManualViewModelAssistedFactoryKey(Factory::class)
    @ContributesIntoMap(AppScope::class)
    interface Factory : ManualViewModelAssistedFactory {
        fun create(eventInfo: EventInfo): EventViewModel
    }

    var eventFullData by mutableStateOf<EventFullData?>(null)
        private set

    var progress by mutableStateOf(false)
        private set

    var error by mutableStateOf<Throwable?>(null)
        private set

    private var job: Job? = null

    init {
        loadData()
    }

    private fun loadData() {
        if (job?.isActive == true) return
        job = viewModelScope.launch {
            try {
                progress = true
                error = null
                eventFullData = apiService.loadEventData(eventInfo.sessionizeDataUrl)
            } catch (e: Throwable) {
                error = e
            } finally {
                progress = false
            }
        }
    }
}