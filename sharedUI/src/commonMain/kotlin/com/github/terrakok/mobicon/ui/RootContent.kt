package com.github.terrakok.mobicon.ui

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.*
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.github.terrakok.mobicon.ui.event.EventPage
import com.github.terrakok.mobicon.ui.events.EventsListPage
import com.github.terrakok.mobicon.ui.session.SessionPage
import com.github.terrakok.mobicon.ui.speaker.SpeakerPage
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

sealed interface AppScreen : NavKey

@Serializable
internal object EventsListScreen : AppScreen

@Serializable
internal data class EventScreen(val id: String) : AppScreen

@Serializable
internal data class SessionScreen(val eventId: String, val id: String) : AppScreen

@Serializable
internal data class SpeakerScreen(val eventId: String, val id: String) : AppScreen

private val config = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(EventsListScreen::class, EventsListScreen.serializer())
            subclass(EventScreen::class, EventScreen.serializer())
            subclass(SessionScreen::class, SessionScreen.serializer())
            subclass(SpeakerScreen::class, SpeakerScreen.serializer())
        }
    }
}

@Composable
internal fun RootContent() {
    val backStack = rememberNavBackStack(config, *getInitialBackStack().toTypedArray())
    BrowserNavigation(backStack)

    NavDisplay(
        backStack = backStack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        onBack = {
            val canGoBack = backStack.size > 1
            if (canGoBack) backStack.removeLast()
        },
        sceneStrategy = rememberDesktopDialogSceneStrategy(),
        transitionSpec = {
            ContentTransform(
                targetContentEnter = EnterTransition.None,
                initialContentExit = ExitTransition.None
            )
        },
        popTransitionSpec = {
            ContentTransform(
                targetContentEnter = EnterTransition.None,
                initialContentExit = ExitTransition.None
            )
        },
        entryProvider = entryProvider {
            entry<EventsListScreen>(
                metadata = DesktopDialogSceneStrategy.dialog()
            ) {
                EventsListPage(
                    onEventClick = {
                        backStack.clear()
                        backStack.add(EventScreen(it))
                    }
                )
            }
            entry<EventScreen> { key ->
                EventPage(
                    eventId = key.id,
                    onSelectConferenceClick = { backStack.add(EventsListScreen) },
                    onSessionClick = { backStack.add(SessionScreen(key.id, it)) }
                )
            }
            entry<SessionScreen>(
                metadata = DesktopDialogSceneStrategy.dialog()
            ) { key ->
                SessionPage(
                    eventId = key.eventId,
                    sessionId = key.id,
                    onSpeakerClick = { backStack.add(SpeakerScreen(key.eventId, it)) },
                    onBack = { backStack.removeLast() }
                )
            }
            entry<SpeakerScreen>(
                metadata = DesktopDialogSceneStrategy.dialog()
            ) { key ->
                SpeakerPage(
                    eventId = key.eventId,
                    speakerId = key.id,
                    onBack = { backStack.removeLast() }
                )
            }
        }
    )
}

internal expect fun getInitialBackStack(): List<NavKey>

@Composable
internal expect fun BrowserNavigation(backStack: NavBackStack<NavKey>)