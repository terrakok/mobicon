package com.github.terrakok.mobicon.ui.event

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.terrakok.mobicon.Colors
import com.github.terrakok.mobicon.EventInfo
import com.github.terrakok.mobicon.Session
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

@Composable
internal fun EventScreen(
    eventInfo: EventInfo
) {
    val vm = assistedMetroViewModel<EventViewModel, EventViewModel.Factory> {
        create(eventInfo)
    }

    val data = vm.eventFullData
    if (data != null) {
        val tz = remember { TimeZone.currentSystemDefault() }
        val days = data.sessions
            .groupBy { session -> session.startsAt.toLocalDateTime(tz).date }
            .map { (date, sessions) ->
                val startTime = sessions.minOf { it.startsAt }.minus(5.minutes)
                val endTime = sessions.maxOf { it.endsAt }.plus(5.minutes)
                val roomAgendas = sessions.groupBy { session -> session.roomId }.map { (roomId, sessions) ->
                    RoomAgenda(roomId, startTime, endTime, sessions.sortedBy { it.startsAt })
                }
                DaySessions(date, roomAgendas)
            }
            .sortedBy { it.date }
        Row { days.first().roomAgendas.forEach { Agenda(Modifier.fillMaxHeight().weight(1f), it)} }
    }
}

private data class DaySessions(
    val date: LocalDate,
    val roomAgendas: List<RoomAgenda>
)

@Immutable
private data class RoomAgenda(
    val roomId: Int?,
    val startTime: Instant,
    val endTime: Instant,
    val sessions: List<Session>
)

@Composable
private fun Agenda(
    modifier: Modifier = Modifier,
    agenda: RoomAgenda
) {
    val lengthInMinutes = remember(agenda) { (agenda.endTime - agenda.startTime).inWholeMinutes }
    Column(modifier = modifier) {
        agenda.sessions.forEachIndexed { index, session ->
            val prev = if (index == 0) agenda.startTime else agenda.sessions[index - 1].endsAt
            val pause = (session.startsAt - prev).inWholeMinutes
            val sessionLen = (session.endsAt - session.startsAt).inWholeMinutes
            if (pause > 0) {
                Spacer(modifier = Modifier.weight(pause.toFloat() / lengthInMinutes))
            }
            Card(
                modifier = Modifier.padding(2.dp).fillMaxWidth().weight(sessionLen.toFloat() / lengthInMinutes),
            ) {
                Column(
                    modifier = Modifier.padding(4.dp),
                ) {
                    Text(session.title)
                }
            }
        }
        val lastPause = remember(agenda) { (agenda.endTime - agenda.sessions.last().endsAt).inWholeMinutes }
        if (lastPause > 0) {
            Spacer(modifier = Modifier.weight(lastPause.toFloat() / lengthInMinutes))
        }
    }
}
