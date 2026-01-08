package com.github.terrakok.mobicon.ui

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.*
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.github.terrakok.mobicon.EventInfo
import com.github.terrakok.mobicon.SessionInfo
import com.github.terrakok.mobicon.Speaker
import com.github.terrakok.mobicon.ui.event.EventScreen
import com.github.terrakok.mobicon.ui.events.EventsListScreen
import com.github.terrakok.mobicon.ui.session.Session
import com.github.terrakok.mobicon.ui.speaker.SpeakerScreen
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

sealed interface AppScreen : NavKey

@Serializable
internal object EventsListScreen : AppScreen

@Serializable
internal data class EventScreen(val info: EventInfo) : AppScreen

@Serializable
internal data class SessionScreen(
    val info: SessionInfo
) : AppScreen

@Serializable
internal data class SpeakerScreen(
    val info: Speaker
) : AppScreen

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
    val backStack = rememberNavBackStack(config, EventsListScreen)
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
                EventsListScreen(
                    onEventClick = {
                        backStack.clear()
                        backStack.add(EventScreen(it))
                    }
                )
            }
            entry<EventScreen> { key ->
                EventScreen(
                    eventInfo = key.info,
                    onSelectConferenceClick = { backStack.add(EventsListScreen) },
                    onSessionClick = { backStack.add(SessionScreen(it)) }
                )
            }
            entry<SessionScreen>(
                metadata = DesktopDialogSceneStrategy.dialog()
            ) { key ->
                Session(
                    data = key.info,
                    onSpeakerClick = { backStack.add(SpeakerScreen(it)) },
                    onBack = { backStack.removeLast() }
                )
            }
            entry<SpeakerScreen>(
                metadata = DesktopDialogSceneStrategy.dialog()
            ) { key ->
                SpeakerScreen(
                    info = key.info,
                    onBack = { backStack.removeLast() }
                )
            }
        }
    )
}

@Composable
internal expect fun BrowserNavigation(backStack: NavBackStack<NavKey>)