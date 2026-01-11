package com.github.terrakok.mobicon.ui.root

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.*
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.github.terrakok.mobicon.ui.DesktopDialogSceneStrategy
import com.github.terrakok.mobicon.ui.event.EventInfoPage
import com.github.terrakok.mobicon.ui.schedule.SchedulePage
import com.github.terrakok.mobicon.ui.events.EventsListPage
import com.github.terrakok.mobicon.ui.rememberDesktopDialogSceneStrategy
import com.github.terrakok.mobicon.ui.session.SessionPage
import com.github.terrakok.mobicon.ui.speaker.SpeakerPage
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

sealed interface AppScreen : NavKey

@Serializable
internal object EventsListScreen : AppScreen

@Serializable
internal data class EventScreen(val id: String) : AppScreen

@Serializable
internal data class EventInfoScreen(val id: String) : AppScreen

@Serializable
internal data class SessionScreen(val eventId: String, val id: String) : AppScreen

@Serializable
internal data class SpeakerScreen(val eventId: String, val id: String) : AppScreen

private val config = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(EventsListScreen::class, EventsListScreen.serializer())
            subclass(EventScreen::class, EventScreen.serializer())
            subclass(EventInfoScreen::class, EventInfoScreen.serializer())
            subclass(SessionScreen::class, SessionScreen.serializer())
            subclass(SpeakerScreen::class, SpeakerScreen.serializer())
        }
    }
}

@Composable
internal fun RootContent() {
    val vm = metroViewModel<RootViewModel>()
    val initialStack = vm.initialStack
    if (initialStack.isEmpty()) return
    val backStack = rememberNavBackStack(config, *initialStack.toTypedArray())
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
                        vm.saveSelectedEvent(it)
                        backStack.clear()
                        backStack.add(EventScreen(it))
                    }
                )
            }
            entry<EventScreen> { key ->
                SchedulePage(
                    eventId = key.id,
                    onSelectConferenceClick = { backStack.add(EventsListScreen) },
                    onSessionClick = { backStack.add(SessionScreen(key.id, it)) },
                    onEventInfoClick = { backStack.add(EventInfoScreen(key.id)) }
                )
            }
            entry<EventInfoScreen>(
                metadata = DesktopDialogSceneStrategy.dialog()
            ) { key ->
                EventInfoPage(
                    eventId = key.id,
                    onBack = { backStack.removeLast() }
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
                    onSessionClick = { backStack.add(SessionScreen(key.eventId, it)) },
                    onBack = { backStack.removeLast() }
                )
            }
        }
    )
}

@Composable
internal expect fun BrowserNavigation(backStack: NavBackStack<NavKey>)