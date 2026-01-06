package com.github.terrakok.mobicon.ui.event

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.github.terrakok.mobicon.*
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import mobicon.sharedui.generated.resources.Res
import mobicon.sharedui.generated.resources.ic_person
import mobicon.sharedui.generated.resources.ic_sentiment
import org.jetbrains.compose.resources.painterResource
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

        val speakers = remember(data.speakers) { data.speakers.associateBy { it.id }.toImmutableMap() }
        val rooms = remember(data.rooms) { data.rooms.associateBy { it.id }.toImmutableMap() }
        val categories = remember(data.categories) {
            data.categories.flatMap { it.items }.associateBy { it.id }.toImmutableMap()
        }

        val days = data.sessions
            .groupBy { session -> session.startsAt.toLocalDateTime(tz).date }
            .map { (date, sessions) ->
                val startTime = sessions.minOf { it.startsAt }
                val endTime = sessions.maxOf { it.endsAt }
                val roomAgendas = sessions.groupBy { session -> session.roomId }.map { (roomId, sessions) ->
                    RoomAgenda(roomId, startTime, endTime, sessions.sortedBy { it.startsAt })
                }
                DaySessions(date, roomAgendas)
            }
            .sortedBy { it.date }
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            days.first().roomAgendas.forEachIndexed { index, it ->
                if (index > 0) Spacer(modifier = Modifier.width(8.dp))
                Agenda(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f),
                    agenda = it,
                    speakers = speakers,
                    rooms = rooms,
                    categories = categories
                )
            }
        }
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
    agenda: RoomAgenda,
    speakers: ImmutableMap<String, Speaker>,
    rooms: ImmutableMap<Int, Room>,
    categories: ImmutableMap<Int, CategoryItem>
) {
    Box(modifier = modifier) {
        val lengthInMinutes = remember(agenda) { (agenda.endTime - agenda.startTime).inWholeMinutes.toInt() }
        Column(modifier = Modifier.height((lengthInMinutes * 9).dp)) {
            agenda.sessions.forEachIndexed { index, session ->
                val prev = if (index == 0) agenda.startTime else agenda.sessions[index - 1].endsAt
                val pause = (session.startsAt - prev).inWholeMinutes
                val sessionLen = (session.endsAt - session.startsAt).inWholeMinutes
                if (pause > 0) {
                    Spacer(modifier = Modifier.weight(pause.toFloat() / lengthInMinutes))
                }

                val sessionSpeaker = session.speakers.firstNotNullOfOrNull { speakers[it] }
                val sessionRoom = rooms[agenda.roomId]
                val sessionCategory = session.categoryItems.firstNotNullOfOrNull { categories[it] }

                SessionCard(
                    session = session,
                    category = sessionCategory,
                    room = sessionRoom,
                    speaker = sessionSpeaker,
                    modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth().weight(sessionLen.toFloat() / lengthInMinutes)
                )
            }
            val lastPause = remember(agenda) { (agenda.endTime - agenda.sessions.last().endsAt).inWholeMinutes }
            if (lastPause > 0) {
                Spacer(modifier = Modifier.weight(lastPause.toFloat() / lengthInMinutes))
            }
        }
    }
}

@Composable
private fun SessionCard(
    session: Session,
    category: CategoryItem?,
    room: Room?,
    speaker: Speaker?,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (category != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = category.name,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = session.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (room != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = room.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.weight(1f))

            if (speaker != null) {
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = speaker.profilePicture,
                        contentDescription = speaker.fullName,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.tertiary),
                        contentScale = ContentScale.Crop,
                        error = painterResource(Res.drawable.ic_sentiment)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = speaker.fullName,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        speaker.tagLine?.let { tagLine ->
                            Text(
                                text = tagLine,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
