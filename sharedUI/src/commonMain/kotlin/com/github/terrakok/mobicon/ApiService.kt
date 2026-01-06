package com.github.terrakok.mobicon

import dev.zacsweers.metro.Inject
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.time.Instant

@Inject
internal class ApiService(
    private val httpClient: HttpClient
) {
    suspend fun loadEvents(): List<EventInfo> {
        val response = httpClient.get(
            "https://firestore.googleapis.com/v1/projects/droidcon-fluttercon-app/databases/(default)/documents/events"
        ).bodyAsText()
        val json = Json.parseToJsonElement(response).jsonObject
        return json["documents"]!!.jsonArray.map {
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
                title = title,
                description = description,
                startDate = LocalDateTime.parse(startDate),
                endDate = LocalDateTime.parse(endDate),
                bannerUrl = bannerUrl,
                venueName = venueName,
                venueAddress = venueAddress,
                sessionizeDataUrl = sessionizeDataUrl
            )
        }
    }

    suspend fun loadEventData(sessionizeDataUrl: String) =
        httpClient.get(sessionizeDataUrl).body<EventFullData>().let { response ->
            response.copy(
                sessions = response.sessions.filter {
                    it.startsAt != LocalDateTime.DISTANT_PAST && it.endsAt != LocalDateTime.DISTANT_PAST
                }
            )
        }
}