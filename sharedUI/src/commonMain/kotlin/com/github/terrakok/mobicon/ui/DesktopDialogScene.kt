package com.github.terrakok.mobicon.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import androidx.window.core.layout.WindowSizeClass
import com.github.terrakok.mobicon.WIDE_SIZE

internal class DesktopDialogScene<T : Any>(
    override val key: Any,
    private val entry: NavEntry<T>,
    override val previousEntries: List<NavEntry<T>>,
    override val overlaidEntries: List<NavEntry<T>>,
    private val dialogProperties: DialogProperties,
    private val onBack: () -> Unit,
) : OverlayScene<T> {

    override val entries: List<NavEntry<T>> = listOf(entry)

    override val content: @Composable (() -> Unit) = {
        Dialog(onDismissRequest = onBack, properties = dialogProperties) {
            Card(
                modifier = Modifier.padding(vertical = 120.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                shape = MaterialTheme.shapes.large
            ) {
                entry.Content()
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as DesktopDialogScene<*>

        return key == other.key &&
                previousEntries == other.previousEntries &&
                overlaidEntries == other.overlaidEntries &&
                entry == other.entry &&
                dialogProperties == other.dialogProperties
    }

    override fun hashCode(): Int {
        return key.hashCode() * 31 +
                previousEntries.hashCode() * 31 +
                overlaidEntries.hashCode() * 31 +
                entry.hashCode() * 31 +
                dialogProperties.hashCode() * 31
    }

    override fun toString(): String {
        return "DesktopDialogScene(key=$key, entry=$entry, previousEntries=$previousEntries, overlaidEntries=$overlaidEntries, dialogProperties=$dialogProperties)"
    }
}

@Composable
internal fun <T : Any> rememberDesktopDialogSceneStrategy(): DesktopDialogSceneStrategy<T> {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    return remember(windowSizeClass) {
        DesktopDialogSceneStrategy(windowSizeClass)
    }
}

internal class DesktopDialogSceneStrategy<T : Any>(
    private val windowSizeClass: WindowSizeClass
) : SceneStrategy<T> {

    override fun SceneStrategyScope<T>.calculateScene(
        entries: List<NavEntry<T>>
    ): Scene<T>? {
        if (!windowSizeClass.isWidthAtLeastBreakpoint(WIDE_SIZE) || entries.size < 2) {
            return null
        }
        val lastEntry = entries.lastOrNull()
        val dialogProperties = lastEntry?.metadata?.get(DIALOG_KEY) as? DialogProperties
        return dialogProperties?.let { properties ->
            DesktopDialogScene(
                key = lastEntry.contentKey.toString() + entries.size,
                entry = lastEntry,
                previousEntries = entries.dropLast(1),
                overlaidEntries = entries.dropLastWhile { it.metadata[DIALOG_KEY] != null },
                dialogProperties = properties,
                onBack = onBack,
            )
        }
    }

    companion object {
        fun dialog(
            dialogProperties: DialogProperties = DialogProperties()
        ): Map<String, Any> = mapOf(DIALOG_KEY to dialogProperties)

        internal const val DIALOG_KEY = "desktop_dialog"
    }
}