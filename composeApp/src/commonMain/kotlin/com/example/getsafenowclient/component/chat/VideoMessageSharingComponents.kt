package com.example.getsafenowclient.component.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.getsafenowclient.room.sharing.VideoRecorderState
import com.example.getsafenowclient.room.sharing.VideoRecorderUiState
import com.example.getsafenowclient.utils.formatTime
import com.example.getsafenowclient.utils.timeText
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Regular
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.regular.PaperPlane
import compose.icons.fontawesomeicons.regular.TimesCircle
import compose.icons.fontawesomeicons.regular.TrashAlt
import compose.icons.fontawesomeicons.solid.Circle
import compose.icons.fontawesomeicons.solid.Play
import compose.icons.fontawesomeicons.solid.Stop
import compose.icons.fontawesomeicons.solid.Video
import io.getsafenow.libraries.designcomponents.utils.preview.GsnPreview
import io.getsafenow.libraries.gsn_theme.customtheme.GsnTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Composable
fun VideoRecordingDialog(
    uiState: VideoRecorderUiState,
    cameraPreview: @Composable () -> Unit,
    onStop: () -> Unit,
    onCancel: () -> Unit,
    onSend: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = GsnTheme.colors.bgCanvasDefault),
            modifier = Modifier
                .fillMaxWidth()
                // Adjust height as needed, or let it wrap. 
                // Figma shows a somewhat square-ish or 4:3 aspect for the content.
                .height(400.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 1. Camera Preview Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color.Black)
                ) {
                    // The Camera Preview Composable passed from platform-specific code
                    cameraPreview()

                    // Overlays
                    // A. Top Left: Recording Badge
                    if (uiState.state == VideoRecorderState.Recording) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.TopStart)
                                .clip(RoundedCornerShape(50))
                                .background(GsnTheme.colors.bgCriticalPrimary) // Red
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = FontAwesomeIcons.Solid.Circle,
                                contentDescription = null,
                                tint = GsnTheme.colors.bgCriticalHovered,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Recording",
                                color = Color.White,
                                style = GsnTheme.typography.fontBodySmMedium
                            )
                        }
                    }

                    // B. Top Right: Close Button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .size(32.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = FontAwesomeIcons.Regular.TimesCircle,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // C. Center/Bottom: Timer Overlay
                    // Figma shows this near the bottom of the video
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(bottom = 22.dp)
                    ) {
                        Text(
                            text = "${formatTime(uiState.durationSeconds)} / ${formatTime(uiState.remainingSeconds)}",
                            color = Color.White,
                            style = GsnTheme.typography.fontHeadingMdBold,
                            fontSize = 24.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    // D. Bottom: Progress Bar
                    LinearProgressIndicator(
                        progress = { uiState.progress },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(6.dp)
                            .padding(bottom = 2.dp),
                        color = GsnTheme.colors.bgCriticalPrimary,
                        trackColor = Color.White.copy(alpha = 0.3f)
                    )
                }

                // 2. Controls Area (White background)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GsnTheme.colors.bgCanvasDefault)
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (uiState.state) {
                        VideoRecorderState.Recording -> {
                            // STOP BUTTON
                            IconButton(
                                onClick = onStop,
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(GsnTheme.colors.bgCriticalPrimary, CircleShape)
                                    .padding(16.dp)
                            ) {
                                Icon(
                                    imageVector = FontAwesomeIcons.Solid.Stop,
                                    contentDescription = "Stop",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        VideoRecorderState.Loading -> {
                            // Processing State
                            CircularProgressIndicator(
                                color = GsnTheme.colors.iconAccentPrimary
                            )
                        }
                        VideoRecorderState.Review -> {
                            // REVIEW MODE (Delete / Send)
                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Delete Button
                                IconButton(
                                    onClick = onCancel,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .background(GsnTheme.colors.bgSubtleSecondary, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = FontAwesomeIcons.Regular.TrashAlt,
                                        contentDescription = "Delete",
                                        tint = GsnTheme.colors.iconPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                // Send Button
                                IconButton(
                                    onClick = onSend,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .background(GsnTheme.colors.bgAccentRest, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = FontAwesomeIcons.Regular.PaperPlane,
                                        contentDescription = "Send",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                        VideoRecorderState.Idle -> {
                            // Starting state - show nothing or loader
                            // We auto-start, so empty is fine to avoid flash
                            Spacer(modifier = Modifier.height(72.dp))
                        }
                    }
                }
            }
        }
    }
}




/**
 * Video Message Bubble for Chat.
 */
@OptIn(ExperimentalTime::class)
@Composable
fun VideoMessageBubble(
    modifier: Modifier = Modifier,
    isMine: Boolean,
    duration: String, // e.g. "0:15"
    timestamp: Long,
    isSending: Boolean = false,
    isError: Boolean = false,
    thumbnail: @Composable () -> Unit = { 
        // Default placeholder if no thumbnail provided
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
             Icon(
                 imageVector = FontAwesomeIcons.Solid.Video,
                 contentDescription = null,
                 tint = Color.White.copy(alpha = 0.5f),
                 modifier = Modifier.size(32.dp)
             )
        }
    },
    onPlayClick: () -> Unit
) {
    val alignment = if (isMine) Alignment.End else Alignment.Start
    val backgroundColor = if (isMine) GsnTheme.colors.bgAccentRest else GsnTheme.colors.bgSubtleSecondary
    
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        // Bubble Content
        Box(
            modifier = Modifier
                .width(240.dp) // Fixed width for video bubble usually looks better
                .aspectRatio(1f) // Square or 4:3 video bubble
                .clip(RoundedCornerShape(16.dp))
                .background(backgroundColor)
                .clickable(onClick = onPlayClick)
        ) {
            // Thumbnail Layer
            thumbnail()
            
            // Play Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f)), // Slight scrim
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = FontAwesomeIcons.Solid.Play,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp).offset(x = 2.dp)
                    )
                }
            }
            
            // Duration Badge (Bottom Left)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .background(Color.Transparent, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = duration,
                    style = GsnTheme.typography.fontBodySmMedium,
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }

        // Status / Time
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

// ------------------------------------------------------------------------
// PREVIEWS
// ------------------------------------------------------------------------

@Preview
@Composable
private fun PreviewVideoRecorderRecording() {
    GsnPreview {
        VideoRecordingDialog(
            uiState = VideoRecorderUiState(
                state = VideoRecorderState.Recording,
                durationSeconds = 5,
                progress = 0.08f
            ),
            cameraPreview = { 
                 Box(Modifier.fillMaxSize().background(Color.DarkGray)) 
            },
            onStop = {},
            onCancel = {},
            onSend = {},
            onDismiss = {}
        )
    }
}

@Preview
@Composable
private fun PreviewVideoRecorderReview() {
    GsnPreview {
        VideoRecordingDialog(
            uiState = VideoRecorderUiState(
                state = VideoRecorderState.Review,
                durationSeconds = 13,
                progress = 0.2f
            ),
            cameraPreview = { 
                Box(Modifier.fillMaxSize().background(Color.Gray)) 
            },
            onStop = {},
            onCancel = {},
            onSend = {},
            onDismiss = {}
        )
    }
}

@Preview
@Composable
private fun PreviewVideoMessageBubble() {
    GsnPreview {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            VideoMessageBubble(
                isMine = true,
                duration = "0:15",
                timestamp = 123456789L,
                onPlayClick = {}
            )
            VideoMessageBubble(
                isMine = false,
                duration = "1:20",
                timestamp = 123456789L,
                onPlayClick = {}
            )
        }
    }
}
