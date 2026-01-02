package com.github.terrakok.mobicon

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Immutable
@Serializable
internal data class EventInfo(
    val id: String,
    val title: String,
    val description: String,
    val startDate: Instant,
    val endDate: Instant,
    val bannerUrl: String,
    val venueName: String,
    val venueAddress: String,
    val sessionizeDataUrl: String
)