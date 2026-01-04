package com.github.terrakok.mobicon.ui

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
    val backStack = rememberNavBackStack(config, EventsList)
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val isWide = remember(windowSizeClass) {
        windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND)
    }
    LaunchedEffect(isWide) {
        if (isWide && backStack.size == 1) {
            backStack.add(EmptyEvent)
        } else if (!isWide && backStack.last() is EmptyEvent) {
            backStack.removeLast()
        }
    }
    BrowserNavigation(backStack)

    NavDisplay(
        backStack = backStack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        onBack = {
            val canGoBack = (isWide && backStack.size > 2) || (!isWide && backStack.size > 1)
            if (canGoBack) backStack.removeLast()
        },
        sceneStrategy = rememberTwoPaneSceneStrategy(),
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
                metadata = TwoPaneScene.twoPane()
            ) {
                EventsListScreen(
                    onEventClick = {
                        backStack.removeAll { it is Event || it is EmptyEvent }
                        backStack.add(Event(it))
                    }
                )
            }
            entry<EmptyEvent>(
                metadata = TwoPaneScene.twoPane()
            ) {
                EmptyEventScreen()
            }
            entry<Event>(
                metadata = TwoPaneScene.twoPane()
            ) { key ->
                EventScreen(key.info)
            }
        }
    )
}

@Composable
internal expect fun BrowserNavigation(backStack: NavBackStack<NavKey>)