package com.example.getsafenowclient.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.getsafenow.libraries.gsn_theme.customtheme.GsnTheme

/**
 * Global sync indicator shown at the top of the screen.
 * Appears when Matrix SDK is syncing data in the background.
 * 
 * @param isSyncing Whether the SDK is currently syncing
 * @param modifier Modifier for positioning (typically Modifier.align(Alignment.TopCenter))
 */
@Composable
fun GlobalSyncIndicator(
    isSyncing: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isSyncing,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(300)
        ) + fadeIn(animationSpec = tween(300)),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(300)
        ) + fadeOut(animationSpec = tween(300)),
        modifier = modifier
    ) {
        LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth(),
            color = GsnTheme.colors.bgAccentRest,
            trackColor = GsnTheme.colors.bgCanvasDefault.copy(alpha = 0.3f)
        )
    }
}
