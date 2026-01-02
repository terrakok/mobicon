package com.github.terrakok.mobicon.ui.events

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.terrakok.mobicon.ApiService
import com.github.terrakok.mobicon.EventInfo
import com.github.terrakok.mobicon.ViewModelKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@ContributesIntoMap(AppScope::class)
@ViewModelKey(EventsListViewModel::class)
@Inject
internal class EventsListViewModel(
    private val api: ApiService
) : ViewModel() {
    var items = mutableStateListOf<EventInfo>()
        private set

    var progress = mutableStateOf(false)
        private set

    var error = mutableStateOf<Throwable?>(null)
        private set

    private var job: Job? = null

    init {
        loadItems()
    }

    fun refresh() {
        loadItems()
    }

    private fun loadItems() {
        if (job?.isActive == true) return
        job = viewModelScope.launch {
            try {
                progress.value = true
                error.value = null
                items.clear()
                items.addAll(api.loadEvents().sortedByDescending { it.startDate })
            } catch (e: Throwable) {
                error.value = e
            } finally {
                progress.value = false
            }
        }
    }
}