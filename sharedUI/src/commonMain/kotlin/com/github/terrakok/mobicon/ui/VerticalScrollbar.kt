package com.github.terrakok.mobicon.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.composables.core.ScrollAreaScope
import com.composables.core.Thumb
import com.composables.core.ThumbVisibility
import com.composables.core.VerticalScrollbar
import kotlin.time.Duration.Companion.seconds

@Composable
internal fun ScrollAreaScope.VerticalScrollbar(padding: PaddingValues) {
    VerticalScrollbar(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(padding)
            .fillMaxHeight()
    ) {
        Thumb(
            modifier = Modifier.background(
                MaterialTheme.colorScheme.onSurface.copy(0.2f),
                RoundedCornerShape(100)
            ),
            thumbVisibility = ThumbVisibility.HideWhileIdle(
                enter = fadeIn(),
                exit = fadeOut(),
                hideDelay = 0.5.seconds
            )
        )
    }
}