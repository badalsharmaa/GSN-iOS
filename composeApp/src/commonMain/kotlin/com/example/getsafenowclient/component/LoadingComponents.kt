package com.example.getsafenowclient.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.getsafenow.libraries.designcomponents.utils.preview.GsnPreview
import io.getsafenow.libraries.gsn_theme.customtheme.GsnTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


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
            // 🔥 Modern loader
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
 * Modern loader with multiple style options.
 * Default: Pulsing dots (Telegram/WhatsApp style)
 */
@Composable
fun GsnLoader(
    modifier: Modifier = Modifier,
    size: Int = 32,
    style: LoaderStyle = LoaderStyle.PulsingDots
) {
    when (style) {
        LoaderStyle.PulsingDots -> PulsingDotsLoader(modifier, size.dp)
        LoaderStyle.BreathingCircle -> BreathingCircleLoader(modifier, size.dp)
        LoaderStyle.SmoothSpinner -> SmoothSpinnerLoader(modifier, size.dp)
    }
}

enum class LoaderStyle {
    PulsingDots,      // Modern, friendly (Telegram, WhatsApp)
    BreathingCircle,  // Minimal, elegant
    SmoothSpinner     // Classic, iOS-like
}


/**
 * Pulsing Dots Loader - Modern Telegram/WhatsApp style
 * Three dots with wave animation
 */
@Composable
private fun PulsingDotsLoader(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "PulsingDots")
    val loaderColor = GsnTheme.colors.bgAccentRest  // ✅ Capture color outside Canvas
    
    val dotSize = size / 8f
    val spacing = size / 6f
    
    Row(
        modifier = modifier.size(size),
        horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, easing = EaseInOutCubic, delayMillis = index * 150),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "DotScale$index"
            )
            
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, easing = EaseInOutCubic, delayMillis = index * 150),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "DotAlpha$index"
            )
            
            Canvas(modifier = Modifier.size(dotSize)) {
                drawCircle(
                    color = loaderColor,  // ✅ Use captured color
                    radius = (dotSize.toPx() / 2f) * scale,
                    alpha = alpha
                )
            }
        }
    }
}


/**
 * Breathing Circle Loader - Minimal, elegant pulsing
 */
@Composable
private fun BreathingCircleLoader(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "BreathingCircle")
    val loaderColor = GsnTheme.colors.bgAccentRest  // ✅ Capture color outside Canvas
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CircleScale"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CircleAlpha"
    )
    
    Canvas(modifier = modifier.size(size)) {
        val radius = (size.toPx() / 2f) * scale
        drawCircle(
            color = loaderColor,  // ✅ Use captured color
            radius = radius,
            alpha = alpha,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}


/**
 * Smooth Spinner Loader - iOS-style rotating arc
 */
@Composable
private fun SmoothSpinnerLoader(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SmoothSpinner")
    val loaderColor = GsnTheme.colors.bgAccentRest  // ✅ Capture color outside Canvas
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SpinnerRotation"
    )
    
    val arcLength by infiniteTransition.animateFloat(
        initialValue = 30f,
        targetValue = 290f,
        animationSpec = infiniteRepeatable(
            animation = tween(1333, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ArcLength"
    )
    
    Canvas(modifier = modifier.size(size)) {
        rotate(rotation) {
            drawArc(
                color = loaderColor,  // ✅ Use captured color
                startAngle = 0f,
                sweepAngle = arcLength,
                useCenter = false,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
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

@Preview
@Composable
private fun LoaderStylesPreview() {
    GsnPreview {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                GsnLoader(size = 42, style = LoaderStyle.PulsingDots)
                Spacer(Modifier.height(8.dp))
                Text("Pulsing Dots", style = GsnTheme.typography.fontBodySmMedium)
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                GsnLoader(size = 42, style = LoaderStyle.BreathingCircle)
                Spacer(Modifier.height(8.dp))
                Text("Breathing Circle", style = GsnTheme.typography.fontBodySmMedium)
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                GsnLoader(size = 42, style = LoaderStyle.SmoothSpinner)
                Spacer(Modifier.height(8.dp))
                Text("Smooth Spinner", style = GsnTheme.typography.fontBodySmMedium)
            }
        }
    }
}
