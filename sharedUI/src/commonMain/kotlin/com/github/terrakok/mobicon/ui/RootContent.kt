package com.github.terrakok.mobicon.ui

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.github.terrakok.mobicon.*
import com.github.terrakok.mobicon.ui.event.EmptyEventScreen
import com.github.terrakok.mobicon.ui.event.EventScreen
import com.github.terrakok.mobicon.ui.events.EventsListScreen
import com.github.terrakok.mobicon.ui.session.Session
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
internal object EmptyEventScreen : AppScreen

private val config = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(EventsListScreen::class, EventsListScreen.serializer())
            subclass(EventScreen::class, EventScreen.serializer())
            subclass(EmptyEventScreen::class, EmptyEventScreen.serializer())
            subclass(SessionScreen::class, SessionScreen.serializer())
        }
    }
}

@Composable
internal fun RootContent() {
    val backStack = rememberNavBackStack(config, EmptyEventScreen, EventsListScreen)
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
            entry<EmptyEventScreen> {
                EmptyEventScreen()
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
                    onBack = { backStack.removeLast() }
                )
            }
        }
    )
}

@Composable
internal expect fun BrowserNavigation(backStack: NavBackStack<NavKey>)