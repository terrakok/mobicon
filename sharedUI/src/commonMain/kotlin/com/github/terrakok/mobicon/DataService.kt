package com.github.terrakok.mobicon

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Inject
@SingleIn(AppScope::class)
internal class DataService(
    private val httpClient: HttpClient
) {
    private val dispatcher = Dispatchers.Default.limitedParallelism(1)

    private val events = mutableMapOf<String, EventInfo>()
    private val eventsData = mutableMapOf<String, EventFullData>()

    suspend fun getEvents(): List<EventInfo> = withContext(dispatcher) {
        if (events.isEmpty()) { loadEvents() }
        events.values.toList().sortedByDescending { it.startDate }
    }

    suspend fun getEventInfo(id: String): EventInfo  = withContext(dispatcher) {
        if (!events.contains(id)) { loadEvents() }
        events[id] ?: error("Event with id $id not found")
    }

    suspend fun getEventFullData(id: String): EventFullData = withContext(dispatcher) {
        if (!eventsData.contains(id)) { loadEventData(id) }
        eventsData[id] ?: error("Event with id $id not found")
    }

    suspend fun getSession(eventId: String, id: String): Session =
        getEventFullData(eventId).sessions.first { it.id == id }

    suspend fun getSpeaker(eventId: String, id: String): Speaker =
        getEventFullData(eventId).speakers.first { it.id == id }

    suspend fun getRoom(eventId: String, id: Int): Room =
        getEventFullData(eventId).rooms.first { it.id == id }

    suspend fun getCategoryItem(eventId: String, id: Int): CategoryItem =
        getEventFullData(eventId).categories.flatMap { it.items }.first { it.id == id }

    private suspend fun loadEvents() {
        val response = httpClient.get(
            "https://firestore.googleapis.com/v1/projects/droidcon-fluttercon-app/databases/(default)/documents/events"
        ).bodyAsText()
        val json = Json.parseToJsonElement(response).jsonObject
        val data = json["documents"]!!.jsonArray.map {
            val item = it.jsonObject["fields"]!!.jsonObject
            val id = item["uniqueId"]!!.jsonObject["stringValue"]!!.jsonPrimitive.content
            val title = item["title"]!!.jsonObject["stringValue"]!!.jsonPrimitive.content.trim()
            val description = item["description"]!!.jsonObject["stringValue"]!!.jsonPrimitive.content.trim()
            val startDate = item["startDate"]!!.jsonObject["stringValue"]!!.jsonPrimitive.content
            val endDate = item["endDate"]!!.jsonObject["stringValue"]!!.jsonPrimitive.content
            val bannerUrl = item["banerUrl"]!!.jsonObject["stringValue"]!!.jsonPrimitive.content
            val venueName = item["venueName"]!!.jsonObject["stringValue"]!!.jsonPrimitive.content
            val venueAddress =
                item["venueAddress"]!!.jsonObject["stringValue"]!!.jsonPrimitive.content.replace("\n", ", ")
            val sessionizeDataUrl = item["sessionizeData"]!!
                .jsonObject["mapValue"]!!
                .jsonObject["fields"]!!
                .jsonObject["allDataUrl"]!!
                .jsonObject["stringValue"]!!
                .jsonPrimitive.content
            EventInfo(
                id = id,
                title = title.replaceFirstChar { it.titlecase() },
                description = description.replaceFirstChar { it.titlecase() },
                startDate = LocalDateTime.parse(startDate),
                endDate = LocalDateTime.parse(endDate),
                bannerUrl = bannerUrl,
                venueName = venueName,
                venueAddress = venueAddress,
                sessionizeDataUrl = sessionizeDataUrl
            )
        }

        events.putAll(data.associateBy { it.id })
    }

    private suspend fun loadEventData(id: String) {
        val url = getEventInfo(id).sessionizeDataUrl
        val data = httpClient.get(url).body<EventFullData>().let { response ->
            response.copy(
                sessions = response.sessions.filter {
                    it.startsAt != LocalDateTime.DISTANT_PAST && it.endsAt != LocalDateTime.DISTANT_PAST
                }
            )
        }
        eventsData[id] = data
    }
}