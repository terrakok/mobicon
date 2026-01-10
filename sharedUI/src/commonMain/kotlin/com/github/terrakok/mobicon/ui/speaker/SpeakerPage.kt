package com.github.terrakok.mobicon.ui.speaker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.github.terrakok.mobicon.Session
import com.github.terrakok.mobicon.Speaker
import com.github.terrakok.mobicon.dayShortString
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import mobicon.sharedui.generated.resources.Res
import mobicon.sharedui.generated.resources.ic_business_center
import mobicon.sharedui.generated.resources.ic_close
import mobicon.sharedui.generated.resources.ic_person
import mobicon.sharedui.generated.resources.ic_raven
import mobicon.sharedui.generated.resources.ic_rss_feed
import mobicon.sharedui.generated.resources.ic_verified
import mobicon.sharedui.generated.resources.ic_web_traffic
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SpeakerPage(
    eventId: String,
    speakerId: String,
    onSessionClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val vm = assistedMetroViewModel<SpeakerViewModel, SpeakerViewModel.Factory> { create(eventId, speakerId) }
    val speaker = vm.speaker ?: return
    val scrollState = rememberScrollState()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        topBar = {
            Surface(
                shadowElevation = if (scrollState.value > 0) 8.dp else 0.dp,
                color = MaterialTheme.colorScheme.surfaceContainerLowest
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(Res.drawable.ic_close), contentDescription = "Close")
                    }
                    Text(
                        text = "Speaker",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = CircleShape,
                shadowElevation = 8.dp,
                modifier = Modifier.size(160.dp)
            ) {
                AsyncImage(
                    model = speaker.profilePicture,
                    contentDescription = speaker.fullName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(Res.drawable.ic_person),
                    error = painterResource(Res.drawable.ic_person)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = speaker.fullName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            speaker.tagLine?.let { tagLine ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = tagLine,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center
                )
            }

            if (speaker.isTopSpeaker) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_verified),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Top Speaker",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            if (speaker.links.isNotEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val uriHandler = LocalUriHandler.current
                    speaker.links.forEach { link ->
                        SocialLinkItem(
                            label = link.title,
                            onClick = { uriHandler.openUri(link.url) }
                        )
                    }
                }
            }

            if (speaker.bio != null) {
                Spacer(modifier = Modifier.height(48.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "About",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = speaker.bio,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 24.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (vm.sessions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Sessions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    vm.sessions.forEach { session ->
                        SpeakerSession(
                            session = session,
                            onSessionClick = onSessionClick,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}


@Composable
private fun SpeakerSession(
    session: Session,
    onSessionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSessionClick(session.id) }
                .padding(12.dp),
        ) {
            Text(
                text = session.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(4.dp))
            Text(
                text = "${session.startsAt.date.dayShortString()}, ${session.startsAt.time} - ${session.endsAt.time}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SocialLinkItem(
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(86.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f),
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(titleToIcon(label)),
                    contentDescription = label,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            minLines = 2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun titleToIcon(title: String) = when {
    title.lowercase().contains("blog") -> Res.drawable.ic_rss_feed
    title.lowercase().contains("twitter") -> Res.drawable.ic_raven
    title.lowercase().contains("linkedin") -> Res.drawable.ic_business_center
    else -> Res.drawable.ic_web_traffic
}