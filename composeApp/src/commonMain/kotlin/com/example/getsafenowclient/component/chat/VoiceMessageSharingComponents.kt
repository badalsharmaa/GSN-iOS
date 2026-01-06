package com.example.getsafenowclient.component.chat

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.getsafenowclient.room.sharing.VoiceRecorderState
import com.example.getsafenowclient.room.sharing.VoiceRecorderUiState
import com.example.getsafenowclient.utils.timeText
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Regular
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.regular.PaperPlane
import compose.icons.fontawesomeicons.regular.TimesCircle
import compose.icons.fontawesomeicons.regular.TrashAlt
import compose.icons.fontawesomeicons.solid.Microphone
import compose.icons.fontawesomeicons.solid.Pause
import compose.icons.fontawesomeicons.solid.Play
import compose.icons.fontawesomeicons.solid.Stop
import io.getsafenow.libraries.designcomponents.utils.preview.GsnPreview
import io.getsafenow.libraries.gsn_theme.customtheme.GsnTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Composable
fun VoiceRecordingDialog(
    uiState: VoiceRecorderUiState,
    onStop: () -> Unit,
    onCancel: () -> Unit,
    onSend: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Close Button (Top Right)
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = FontAwesomeIcons.Regular.TimesCircle,
                            contentDescription = "Close",
                            tint = GsnTheme.colors.iconSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Animation logic for pulse
                val infiniteTransition = rememberInfiniteTransition()
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )
                val isRecording = uiState.state == VoiceRecorderState.Recording

                // 2. Microphone Visualizer
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(80.dp)
                        .scale(if (isRecording) pulseScale else 1f)
                        .clip(CircleShape)
                        .background(Color(0xFFFFEBEE)) // Light Red
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFCDD2)) // Slightly darker red
                    ) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.Microphone,
                            contentDescription = "Microphone",
                            tint = GsnTheme.colors.iconCriticalPrimary, // Red
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Status Text (Recording Mode only)
                if (uiState.state == VoiceRecorderState.Recording) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD32F2F))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Recording",
                            color = GsnTheme.colors.textCriticalPrimary,
                            fontSize = 14.sp,
                            style = GsnTheme.typography.fontBodyMdMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // 4. Timer
                Text(
                    text = formatTime(uiState.durationSeconds),
                    color = GsnTheme.colors.textPrimary,
                    fontSize = 32.sp,
                    style = GsnTheme.typography.fontHeadingMdBold
                )
                Text(
                    text = "${formatTime(uiState.remainingSeconds)} remaining",
                    color = GsnTheme.colors.textSecondary,
                    fontSize = 12.sp,
                    style = GsnTheme.typography.fontBodySmRegular
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 5. Progress Bar
                LinearProgressIndicator(
                    progress = {uiState.progress},
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color.Black,
                    trackColor = Color.LightGray
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 6. Controls
                if (uiState.state == VoiceRecorderState.Recording) {
                    // STOP BUTTON
                    IconButton(
                        onClick = onStop,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD32F2F))
                    ) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.Stop,
                            contentDescription = "Stop",
                            tint = GsnTheme.colors.bgCanvasDefault,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else {
                    // REVIEW MODE (Delete / Send)
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = onCancel,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF5F5F5)) // Light Gray
                        ) {
                            Icon(
                                imageVector = FontAwesomeIcons.Regular.TrashAlt,
                                contentDescription = "Delete",
                                tint = GsnTheme.colors.iconPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onSend,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(GsnTheme.colors.bgAccentRest) // Blue
                        ) {
                            Icon(
                                imageVector = FontAwesomeIcons.Regular.PaperPlane,
                                contentDescription = "Send",
                                tint = GsnTheme.colors.bgCanvasDefault,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "${m}:${s.toString().padStart(2, '0')}"
}

/**
 * Chat Bubble for Voice Messages.
 */
@OptIn(ExperimentalTime::class)
@Composable
fun VoiceMessageBubble(
    modifier: Modifier = Modifier,
    durationMs: Long,
    currentPositionMs: Long,
    isPlaying: Boolean,
    isMine: Boolean,
    onPlayPauseClick: () -> Unit,
    timestamp: Long = 0L,
    isSending: Boolean = false,
    isError: Boolean = false
) {
    val backgroundColor = if (isMine) GsnTheme.colors.bgAccentRest else GsnTheme.colors.bgSubtleSecondary
    val contentColor = if (isMine) GsnTheme.colors.iconOnSolidPrimary else GsnTheme.colors.iconPrimary
    val trackColor = contentColor.copy(alpha = 0.3f)
    val alignment = if (isMine) Alignment.End else Alignment.Start
    val textColor = if (isMine) GsnTheme.colors.bgCanvasDefault else GsnTheme.colors.textSecondary

    val progress = if (durationMs > 0) currentPositionMs.toFloat() / durationMs.toFloat() else 0f

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Row(
            modifier = Modifier
                .width(240.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(backgroundColor)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Play/Pause
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = 0.1f))
                    .clickable(onClick = onPlayPauseClick)
            ) {
                Icon(
                    imageVector = if (isPlaying) FontAwesomeIcons.Solid.Pause else FontAwesomeIcons.Solid.Play,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = contentColor,
                    modifier = Modifier.size(16.dp).offset(x = if (isPlaying) 0.dp else 2.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Progress Bar
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = contentColor,
                    trackColor = trackColor
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Duration Text
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTimeMs(currentPositionMs),
                        style = GsnTheme.typography.fontBodySmMedium,
                        color = textColor
                    )
                    Text(
                        text = formatTimeMs(durationMs),
                        style = GsnTheme.typography.fontBodySmMedium,
                        color = textColor
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Micro Icon
            Icon(
                imageVector = FontAwesomeIcons.Solid.Microphone,
                contentDescription = "Voice Message",
                tint = contentColor.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
            )
        }

        // 🔥 Status Text (Time / Sending / Error)
        if (timestamp != 0L || isSending || isError) {
            val statusText = when {
                isError -> "Error"
                isSending -> "Sending..."
                else -> Instant.fromEpochMilliseconds(timestamp).timeText()
            }
            val statusColor = if (isError) GsnTheme.colors.textCriticalPrimary else GsnTheme.colors.textSecondary

            Text(
                text = statusText,
                style = GsnTheme.typography.fontBodySmMedium,
                color = statusColor,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

private fun formatTimeMs(millis: Long): String {
    val totalSeconds = millis / 1000
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return "${m}:${s.toString().padStart(2, '0')}"
}




// ------------------------------------------------------------------------
// PREVIEWS
// ------------------------------------------------------------------------

@Preview
@Composable
private fun PreviewRecordingState() {
    GsnPreview {
        VoiceRecordingDialog(
            uiState = VoiceRecorderUiState(
                state = VoiceRecorderState.Recording,
                durationSeconds = 2,
                maxDurationSeconds = 60,
                progress = 0.03f
            ),
            onStop = {},
            onCancel = {},
            onSend = {},
            onDismiss = {}
        )
    }
}

@Preview
@Composable
private fun PreviewReviewState() {
    GsnPreview {
        VoiceRecordingDialog(
            uiState = VoiceRecorderUiState(
                state = VoiceRecorderState.Review,
                durationSeconds = 10,
                maxDurationSeconds = 60,
                progress = 0.16f
            ),
            onStop = {},
            onCancel = {},
            onSend = {},
            onDismiss = {}
        )
    }
}

@Preview
@Composable
private fun PreviewVoiceBubbleMine() {
    GsnPreview {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            VoiceMessageBubble(
                durationMs = 35000,
                currentPositionMs = 0,
                isPlaying = false,
                isMine = true,
                onPlayPauseClick = {}
            )
            VoiceMessageBubble(
                durationMs = 60000,
                currentPositionMs = 25000,
                isPlaying = true,
                isMine = false,
                onPlayPauseClick = {}
            )
        }
    }
}
