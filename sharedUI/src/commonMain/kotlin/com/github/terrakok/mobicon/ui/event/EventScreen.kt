package com.github.terrakok.mobicon.ui.event

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.github.terrakok.mobicon.EventInfo

@Composable
internal fun EventScreen(
    eventInfo: EventInfo
) {
    Text(eventInfo.title)
}

@Composable
internal fun EmptyEventScreen() {
    Text("Select event from the list")
}