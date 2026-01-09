package com.github.terrakok.mobicon

import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

private val past = LocalDateTime(1, 1, 1, 0, 0, 0)
internal val LocalDateTime.Companion.DISTANT_PAST get() = past

@Immutable
@Serializable
internal data class EventInfo(
    val id: String,
    val title: String,
    val description: String,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val bannerUrl: String,
    val venueName: String,
    val venueAddress: String,
    val sessionizeDataUrl: String
)

@Immutable
@Serializable
data class EventFullData(
    @SerialName("sessions")
    val sessions: List<Session>,
    @SerialName("speakers")
    val speakers: List<Speaker>,
    @SerialName("categories")
    val categories: List<Category>,
    @SerialName("rooms")
    val rooms: List<Room>
)

@Immutable
@Serializable
data class Session(
    @SerialName("id")
    val id: String,
    @SerialName("title")
    val title: String,
    @SerialName("description")
    val description: String?,
    @SerialName("startsAt")
    val startsAt: LocalDateTime = LocalDateTime.DISTANT_PAST,
    @SerialName("endsAt")
    val endsAt: LocalDateTime = LocalDateTime.DISTANT_PAST,
    @SerialName("isServiceSession")
    val isServiceSession: Boolean,
    @SerialName("isPlenumSession")
    val isPlenumSession: Boolean,
    @SerialName("speakers")
    val speakers: List<String>,
    @SerialName("categoryItems")
    val categoryItems: List<Int>,
    @SerialName("roomId")
    val roomId: Int?,
    @SerialName("liveUrl")
    val liveUrl: String?,
    @SerialName("recordingUrl")
    val recordingUrl: String?,
    @SerialName("status")
    val status: String?,
    @SerialName("isInformed")
    val isInformed: Boolean,
    @SerialName("isConfirmed")
    val isConfirmed: Boolean
)

@Immutable
@Serializable
data class Speaker(
    @SerialName("id")
    val id: String,
    @SerialName("firstName")
    val firstName: String,
    @SerialName("lastName")
    val lastName: String,
    @SerialName("bio")
    val bio: String?,
    @SerialName("tagLine")
    val tagLine: String?,
    @SerialName("profilePicture")
    val profilePicture: String?,
    @SerialName("isTopSpeaker")
    val isTopSpeaker: Boolean,
    @SerialName("links")
    val links: List<Link>,
    @SerialName("sessions")
    val sessions: List<Int>,
    @SerialName("fullName")
    val fullName: String,
)

@Serializable
data class Link(
    @SerialName("title")
    val title: String,
    @SerialName("url")
    val url: String,
    @SerialName("linkType")
    val linkType: String
)

@Immutable
@Serializable
data class Category(
    @SerialName("id")
    val id: Int,
    @SerialName("title")
    val title: String,
    @SerialName("items")
    val items: List<CategoryItem>,
    @SerialName("sort")
    val sort: Int,
    @SerialName("type")
    val type: String
)

@Serializable
data class CategoryItem(
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String,
    @SerialName("sort")
    val sort: Int
)

@Serializable
data class Room(
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String,
    @SerialName("sort")
    val sort: Int
)