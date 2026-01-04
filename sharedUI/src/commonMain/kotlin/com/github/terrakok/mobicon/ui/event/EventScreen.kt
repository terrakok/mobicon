package com.github.terrakok.mobicon.ui.event

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.github.terrakok.mobicon.EventInfo
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel

@Composable
internal fun EventScreen(
    eventInfo: EventInfo
) {
    val vm = assistedMetroViewModel<EventViewModel, EventViewModel.Factory> {
        create(eventInfo)
    }

    val data = vm.eventFullData
    if (data != null) {
        Text("Sessions' count ${data.sessions.size}")
    }
}
