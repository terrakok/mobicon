package com.github.terrakok.mobicon.ui.event

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.github.terrakok.mobicon.*
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import mobicon.sharedui.generated.resources.Res
import mobicon.sharedui.generated.resources.ic_arrow_drop_down
import mobicon.sharedui.generated.resources.ic_sentiment
import org.jetbrains.compose.resources.painterResource
import kotlin.text.toFloat
import kotlin.time.Duration.Companion.seconds

@Composable
internal fun EventScreen(
    eventInfo: EventInfo,
    onSelectConferenceClick: () -> Unit,
    onSessionClick: (SessionInfo) -> Unit,
) {
    val vm = assistedMetroViewModel<EventViewModel, EventViewModel.Factory> {
        create(eventInfo)
    }

    val data = vm.eventFullData
    if (data != null) {
        val speakers = remember(data.speakers) { data.speakers.associateBy { it.id } }
        val rooms = remember(data.rooms) { data.rooms.associateBy { it.id } }
        val categories = remember(data.categories) { data.categories.flatMap { it.items }.associateBy { it.id } }

        val days = data.sessions
            .groupBy { session -> session.startsAt.date }
            .map { (date, sessions) ->
                val startTime = sessions.minOf { it.startsAt.time }
                val endTime = sessions.maxOf { it.endsAt.time }
                val roomAgendas = sessions.groupBy { session -> session.roomId }.map { (roomId, sessions) ->
                    RoomAgenda(
                        roomId = roomId,
                        startTime = startTime,
                        endTime = endTime,
                        sessions = sessions.sortedBy { it.startsAt }
                    )
                }
                DaySessions(
                    date = date,
                    roomAgendas = roomAgendas,
                    speakers = speakers,
                    rooms = rooms,
                    categories = categories
                )
            }
            .sortedBy { it.date }

        var selectedDay by remember { mutableStateOf(days.first()) }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Header(
                eventInfo = eventInfo,
                days = days,
                selectedDay = selectedDay,
                onDaySelect = { selectedDay = it },
                onSelectConferenceClick = onSelectConferenceClick
            )
            val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
            val isWide = windowSizeClass.isWidthAtLeastBreakpoint(WIDE_SIZE)
            val onSessionClick: (Session) -> Unit = { session ->
                val info = SessionInfo(
                    session = session,
                    speakers = speakers.entries.filter { session.speakers.contains(it.key) }.map { it.value },
                    room = rooms[session.roomId]!!,
                    categories = categories.entries.filter { session.categoryItems.contains(it.key) }.map { it.value }
                )
                onSessionClick(info)
            }
            if (isWide) {
                WideScreenSchedule(selectedDay, onSessionClick)
            } else {
                NarrowScreenSchedule(selectedDay, onSessionClick)
            }
        }
    }
}

@Immutable
private data class DaySessions(
    val date: LocalDate,
    val roomAgendas: List<RoomAgenda>,
    val speakers: Map<String, Speaker>,
    val rooms: Map<Int, Room>,
    val categories: Map<Int, CategoryItem>,
)

@Immutable
private data class TimelineData(
    val begin: LocalTime,
    val end: LocalTime,
    val times: List<LocalTime>
)

@Immutable
private data class RoomAgenda(
    val roomId: Int?,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val sessions: List<Session>
)

@Composable
private fun Agenda(
    modifier: Modifier = Modifier,
    agenda: RoomAgenda,
    speakers: Map<String, Speaker>,
    rooms: Map<Int, Room>,
    categories: Map<Int, CategoryItem>,
    onSessionClick: (Session) -> Unit
) {
    Column(modifier = modifier) {
        val lengthInMinutes = agenda.endTime - agenda.startTime
        agenda.sessions.forEachIndexed { index, session ->
            val prev = if (index == 0) agenda.startTime else agenda.sessions[index - 1].endsAt.time
            val pause = session.startsAt.time - prev
            val sessionLen = session.endsAt.time - session.startsAt.time
            if (pause > 0) {
                Spacer(modifier = Modifier.weight(pause.toFloat() / lengthInMinutes))
            }

            val sessionRoom = rooms[agenda.roomId]
            if (!session.isServiceSession) {
                val sessionSpeaker = session.speakers.firstNotNullOfOrNull { speakers[it] }
                val sessionCategory = session.categoryItems.firstNotNullOfOrNull { categories[it] }

                SessionCard(
                    session = session,
                    category = sessionCategory,
                    room = sessionRoom,
                    speaker = sessionSpeaker,
                    onSessionClick = onSessionClick,
                    modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth()
                        .weight(sessionLen.toFloat() / lengthInMinutes)
                )
            } else {
                ServiceSessionCard(
                    session = session,
                    room = sessionRoom,
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .fillMaxWidth()
                        .weight(sessionLen.toFloat() / lengthInMinutes)
                )
            }
        }
        val lastPause = agenda.endTime - agenda.sessions.last().endsAt.time
        if (lastPause > 0) {
            Spacer(modifier = Modifier.weight(lastPause.toFloat() / lengthInMinutes))
        }
    }
}


internal operator fun LocalTime.minus(other: LocalTime) =
    (toSecondOfDay() - other.toSecondOfDay()).seconds.inWholeMinutes

@Composable
private fun Timeline(
    data: TimelineData,
    modifier: Modifier = Modifier,
) {
    Layout(
        modifier = modifier,
        content = {
            data.times.forEach { time ->
                Text(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    text = time.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp
                )
            }
        },
        measurePolicy = { measurables, constraints ->
            val lengthInMinutes = data.end - data.begin
            val placeables = measurables.map { it.measure(constraints) }

            layout(constraints.maxWidth, constraints.maxHeight) {
                placeables.forEachIndexed { index, placeable ->
                    val time = data.times[index]
                    val offset = time - data.begin
                    val y = (constraints.maxHeight * (offset.toFloat() / lengthInMinutes)).toInt()
                    placeable.placeRelative(0, y - 25)
                }
            }
        }
    )
}

@Composable
private fun DaySelector(
    modifier: Modifier = Modifier,
    days: List<DaySessions>,
    selectedDay: DaySessions,
    onSelect: (DaySessions) -> Unit
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        items(days) { day ->
            DayItem(
                day = day,
                isSelected = day == selectedDay,
                onClick = { onSelect(day) }
            )
        }
    }
}

@Composable
private fun DayItem(
    day: DaySessions,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val text = day.date.let { date ->
        val dayOfWeek = date.dayOfWeek.name.lowercase().take(3).replaceFirstChar { it.uppercase() }
        val month = date.month.name.lowercase().take(3).replaceFirstChar { it.uppercase() }
        "$dayOfWeek, $month ${date.day}"
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        )
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(3.dp)
                .background(
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)
                )
        )
    }
}

@Composable
private fun SessionCard(
    session: Session,
    category: CategoryItem?,
    room: Room?,
    speaker: Speaker?,
    onSessionClick: (Session) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .clickable { onSessionClick(session) }
                .padding(12.dp),
        ) {
            if (category != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = category.name,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Column {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${session.startsAt.time} - ${session.endsAt.time}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (room != null) {
                        Text(
                            text = room.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

            }

            if (speaker != null) {
                Spacer(Modifier.height(4.dp))
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

@Composable
private fun Header(
    eventInfo: EventInfo,
    days: List<DaySessions>,
    selectedDay: DaySessions,
    onDaySelect: (DaySessions) -> Unit,
    onSelectConferenceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .clip(RoundedCornerShape(50))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = { onSelectConferenceClick() }
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = eventInfo.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_drop_down),
                contentDescription = null,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        if (days.size > 1) {
            DaySelector(
                days = days,
                selectedDay = selectedDay,
                onSelect = onDaySelect
            )
        }
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun WideScreenSchedule(
    selectedDay: DaySessions,
    onSessionClick: (Session) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        val daySessions = selectedDay.roomAgendas.flatMap { it.sessions }
        val begin = daySessions.minOf { it.startsAt.time }
        val end = daySessions.maxOf { it.endsAt.time }
        val shortest = daySessions
            .filter { !it.isServiceSession }
            .minOf { it.endsAt.time - it.startsAt.time }
        val height = ((160 * (end - begin)) / shortest).toInt()

        val times = daySessions
            .map { it.startsAt.time }
            .distinct()
            .sorted()

        Timeline(TimelineData(begin, end, times), Modifier.width(60.dp).height(height.dp))
        selectedDay.roomAgendas.forEachIndexed { index, it ->
            if (index > 0) Spacer(modifier = Modifier.width(8.dp))
            Agenda(
                modifier = Modifier.height(height.dp).weight(1f),
                agenda = it,
                speakers = selectedDay.speakers,
                rooms = selectedDay.rooms,
                categories = selectedDay.categories,
                onSessionClick = onSessionClick
            )
        }
    }
}

@Composable
private fun NarrowScreenSchedule(
    selectedDay: DaySessions,
    onSessionClick: (Session) -> Unit,
) {
    val daySessions = selectedDay.roomAgendas
        .flatMap { it.sessions }
        .sortedBy { it.startsAt }
        .groupBy { it.startsAt }
        .flatMap { (time, sessions) -> listOf(time) + sessions }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(daySessions) { item ->
            if (item is LocalDateTime) {
                Text(
                    text = item.time.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp
                )
            } else if (item is Session) {
                if (item.isServiceSession) {
                    ServiceSessionCard(
                        session = item,
                        room = selectedDay.rooms[item.roomId],
                        modifier = Modifier.fillMaxWidth().height(200.dp)
                    )
                } else {
                    SessionCard(
                        session = item,
                        category = selectedDay.categories[item.categoryItems.firstOrNull()],
                        room = selectedDay.rooms[item.roomId],
                        speaker = selectedDay.speakers[item.speakers.firstOrNull()],
                        onSessionClick = onSessionClick,
                        modifier = Modifier.fillMaxWidth().height(200.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ServiceSessionCard(
    session: Session,
    room: Room?,
    modifier: Modifier = Modifier,
) {
    val borderColor = MaterialTheme.colorScheme.outlineVariant
    Column(
        modifier = modifier
            .drawBehind {
                val strokeWidthValue = 2.dp
                val cornerRadiusValue = 16.dp
                drawRoundRect(
                    color = borderColor,
                    style = Stroke(
                        width = strokeWidthValue.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    ),
                    cornerRadius = CornerRadius(cornerRadiusValue.toPx())
                )
            }
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = session.title,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        Text(
            text = room?.name.orEmpty(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}