package com.github.terrakok.mobicon.ui.events

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.terrakok.mobicon.DataService
import com.github.terrakok.mobicon.EventInfo
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.launch

@ContributesIntoMap(AppScope::class)
@ViewModelKey(EventsListViewModel::class)
@Inject
internal class EventsListViewModel(
    private val dataService: DataService
) : ViewModel() {
    var items = mutableStateListOf<EventInfo>()
        private set

    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    init {
        loadItems()
    }

    fun loadItems() {
        viewModelScope.launch {
            try {
                loading = true
                error = null
                items.clear()
                items.addAll(dataService.getEvents())
            } catch (e: Throwable) {
                error = e.message
            } finally {
                loading = false
            }
        }
    }
}