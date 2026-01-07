package com.github.terrakok.mobicon.ui

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import com.github.terrakok.mobicon.EventInfo
import com.github.terrakok.mobicon.ui.event.EmptyEventScreen
import com.github.terrakok.mobicon.ui.event.EventScreen
import com.github.terrakok.mobicon.ui.events.EventsListScreen
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

sealed interface AppScreen : NavKey

@Serializable
internal object EventsList : AppScreen

@Serializable
internal data class Event(val info: EventInfo) : AppScreen

@Serializable
internal object EmptyEvent : AppScreen

private val config = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(EventsList::class, EventsList.serializer())
            subclass(Event::class, Event.serializer())
            subclass(EmptyEvent::class, EmptyEvent.serializer())
        }
    }
}

@Composable
internal fun RootContent() {
    val backStack = rememberNavBackStack(config, EmptyEvent, EventsList)
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
            entry<EventsList>(
                metadata = DesktopDialogSceneStrategy.dialog()
            ) {
                EventsListScreen(
                    onEventClick = {
                        backStack.clear()
                        backStack.add(Event(it))
                    }
                )
            }
            entry<EmptyEvent> {
                EmptyEventScreen()
            }
            entry<Event> { key ->
                EventScreen(
                    eventInfo = key.info,
                    onSelectConferenceClick = { backStack.add(EventsList) }
                )
            }
        }
    )
}

@Composable
internal expect fun BrowserNavigation(backStack: NavBackStack<NavKey>)