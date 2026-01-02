package com.github.terrakok.mobicon.ui.events

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.terrakok.mobicon.Colors
import com.github.terrakok.mobicon.EventInfo
import com.github.terrakok.mobicon.metroVmScoped
import com.materialkolor.ktx.harmonize
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun EventsListScreen(
    onEventClick: (EventInfo) -> Unit
) {
    val vm = metroVmScoped<EventsListViewModel>()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(8.dp),
    ) {
        items(vm.items, key = { it.id }) { event ->
            EventInfoCard(event, onEventClick)
        }
    }
}

private val dateFormat = LocalDate.Format {
    day(padding = Padding.NONE)
    char(' ')
    monthName(MonthNames.ENGLISH_FULL)
}

@Composable
private fun EventInfoCard(
    info: EventInfo,
    onClick: (EventInfo) -> Unit
) {
    val cardBgColor = remember(info.id) { Colors.getForString(info.id) }
    Card(
        modifier = Modifier.fillMaxWidth().height(200.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = cardBgColor
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick(info) }
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = info.title.replaceFirstChar { it.titlecase() },
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onPrimary.harmonize(cardBgColor)
                )
                Text(text = info.venueName)
                val start = info.startDate.toLocalDateTime(TimeZone.currentSystemDefault()).date
                val end = info.endDate.toLocalDateTime(TimeZone.currentSystemDefault()).date

                val dateText = if (start != end) {
                    if (start.month != end.month) {
                        "${dateFormat.format(start)} - ${dateFormat.format(end)}"
                    } else {
                        "${start.day} - ${dateFormat.format(end)}"
                    }
                } else {
                    dateFormat.format(start)
                }
                Text(dateText + " " + end.year.toString())

                Spacer(Modifier.weight(1f))
                Text(
                    text = info.venueAddress,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}