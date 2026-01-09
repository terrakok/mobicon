package com.github.terrakok.mobicon.ui.events

import androidx.compose.runtime.mutableStateListOf
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

    init {
        loadItems()
    }

    private fun loadItems() {
        viewModelScope.launch {
            try {
                items.clear()
                items.addAll(dataService.getEvents())
            } catch (e: Throwable) {
            }
        }
    }
}