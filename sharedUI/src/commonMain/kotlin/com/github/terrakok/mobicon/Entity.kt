package com.github.terrakok.mobicon

import androidx.compose.runtime.Immutable
import kotlin.time.Instant

@Immutable
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