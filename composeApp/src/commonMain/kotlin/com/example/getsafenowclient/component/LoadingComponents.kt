package com.example.getsafenowclient.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LoadingIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.getsafenow.libraries.designcomponents.utils.preview.GsnPreview
import io.getsafenow.libraries.gsn_theme.customtheme.GsnTheme
import org.jetbrains.compose.ui.tooling.preview.Preview


/**
 * Common full-screen loading UI for GetSafeNow.
 *
 * @param modifier Screen modifier.
 * @param loadingMessage Optional animated text below the loader.
 */
@Composable
fun LoadingScreen(
    modifier: Modifier = Modifier,
    loadingMessage: String? = null
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = GsnTheme.colors.bgCanvasDefault
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 🔥 Shared loader used everywhere
            GsnLoader(size = 42)

            if (loadingMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))

                AnimatedContent(
                    targetState = loadingMessage,
                    transitionSpec = {
                        (slideInVertically(animationSpec = tween(400)) { it } + fadeIn(tween(400)))
                            .togetherWith(
                                slideOutVertically(animationSpec = tween(400)) { -it } + fadeOut(tween(400))
                            )
                    },
                    label = "LoadingScreenMessage"
                ) { text ->
                    Text(
                        text = text,
                        style = GsnTheme.typography.fontBodyMdMedium,
                        color = GsnTheme.colors.textSecondary
                    )
                }
            }
        }
    }
}


/**
 * Centralised loader used across app.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GsnLoader(
    modifier: Modifier = Modifier,
    size: Int = 32
) {
    LoadingIndicator(
        modifier = modifier.size(size.dp),
        color = GsnTheme.colors.bgAccentRest,
        polygons = LoadingIndicatorDefaults.IndeterminateIndicatorPolygons
    )
}


@Preview
@Composable
private fun LoadingScreenPreview() {
    GsnPreview {
        LoadingScreen()
    }
}

@Preview
@Composable
private fun LoadingScreenWithMessagePreview() {
    GsnPreview {
        LoadingScreen(loadingMessage = "Loading messages...")
    }
}
