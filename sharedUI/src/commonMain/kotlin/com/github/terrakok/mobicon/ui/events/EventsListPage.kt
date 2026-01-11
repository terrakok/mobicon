package com.github.terrakok.mobicon.ui.events

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.terrakok.mobicon.EventInfo
import com.github.terrakok.mobicon.dateFormat
import com.github.terrakok.mobicon.timeFormat
import com.github.terrakok.mobicon.ui.LoadingWidget
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import mobicon.sharedui.generated.resources.Res
import mobicon.sharedui.generated.resources.ic_calendar
import mobicon.sharedui.generated.resources.ic_close
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun EventsListPage(
    onEventClick: (String) -> Unit
) {
    val vm = metroViewModel<EventsListViewModel>()

    LoadingWidget(
        modifier = Modifier.fillMaxSize(),
        error = vm.error,
        loading = vm.loading,
        onReload = { vm.loadItems() }
    )
    if (vm.loading || vm.error != null) return

    val scrollState = rememberLazyListState()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        topBar = {
            Surface(
                shadowElevation = if (scrollState.canScrollBackward) 8.dp else 0.dp,
                color = MaterialTheme.colorScheme.surfaceContainerLowest
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(
                            WindowInsets.safeDrawing
                                .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                        ),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "Select conference",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center).padding(horizontal = 60.dp, vertical = 20.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainer),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = padding.plus(PaddingValues(16.dp)),
            state = scrollState
        ) {
            itemsIndexed(vm.items, key = { _, e -> e.id }) { index, event ->
                val y = event.startDate.year
                if (index == 0 || y != vm.items[index - 1].startDate.year) {
                    Text(
                        text = y.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    )
                }
                EventInfoCard(event, onEventClick)
            }
        }
    }
}

@Composable
private fun EventInfoCard(
    info: EventInfo,
    onClick: (String) -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick(info.id) }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = info.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = info.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = info.venueName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = info.venueAddress,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            val start = info.startDate.date
            val end = info.endDate.date

            val dateText = if (start != end) {
                if (start.month != end.month) {
                    "${dateFormat.format(start)} - ${dateFormat.format(end)}"
                } else {
                    "${dateFormat.format(start)} - ${end.day}"
                }
            } else {
                dateFormat.format(start)
            }
            val timeText = timeFormat.format(info.startDate.time)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(Res.drawable.ic_calendar),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$dateText • $timeText",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}