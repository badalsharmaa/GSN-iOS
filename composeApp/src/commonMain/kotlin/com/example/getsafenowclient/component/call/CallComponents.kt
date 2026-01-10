package com.example.getsafenowclient.component.call

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.getsafenowclient.call.CallState
import com.example.getsafenowclient.call.CallUiState
import com.example.getsafenowclient.call.EndCallReason
import com.example.getsafenowclient.common.ui.GsnAvatarAdvanced
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ChevronDown
import compose.icons.fontawesomeicons.solid.Microphone
import compose.icons.fontawesomeicons.solid.MicrophoneSlash
import compose.icons.fontawesomeicons.solid.Phone
import compose.icons.fontawesomeicons.solid.PhoneSlash
import compose.icons.fontawesomeicons.solid.Sync
import compose.icons.fontawesomeicons.solid.Video
import compose.icons.fontawesomeicons.solid.VideoSlash
import compose.icons.fontawesomeicons.solid.VolumeMute
import compose.icons.fontawesomeicons.solid.VolumeUp
import io.getsafenow.libraries.designcomponents.utils.preview.GsnPreview
import io.getsafenow.libraries.gsn_theme.customtheme.GsnTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import net.folivo.trixnity.client.MatrixClient
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Standard layout for Video Calls:
 * - Remote Video: Fills the screen
 * - Local Video: Floating Picture-in-Picture (Top Right)
 */
@Composable
fun CallVideoLayout(
    remoteVideo: @Composable () -> Unit,
    localVideo: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        // 1. Remote Stream (Full Screen)
        remoteVideo()

        // 2. Local Stream (Floating Pip)
        // Only show if we have a local stream
        if (localVideo != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 64.dp, end = 16.dp) // Avoid top bar
                    .width(100.dp)
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.DarkGray)
                    .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                localVideo()
            }
        }
    }
}

/**
 * Minimized Call Banner to be shown on top of other screens.
 */
@Composable
fun CallOverlayBanner(
    state: CallUiState,
    client: MatrixClient?,
    onExpandClick: () -> Unit,
    onEndCallClick: () -> Unit,
    onAcceptClick: () -> Unit,
    onToggleMic: () -> Unit,
    onToggleSpeaker: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Green or Accent color for active call indication
    val backgroundColor = GsnTheme.colors.bgAccentRest

    Surface(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            .height(72.dp)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable(onClick = onExpandClick),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            // Avatar
            GsnAvatarAdvanced(
                modifier = Modifier.size(48.dp),
                id = state.roomId ?: "",
                name = state.peerName ?: "Unknown",
                url = state.peerAvatarUrl,
                client = client
            )

            // Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = state.peerName ?: "Unknown",
                    style = GsnTheme.typography.fontBodyLgMedium,
                    color = GsnTheme.colors.textOnSolidPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Dynamic Status Text
                CallStatusText(
                    state = state,
                    style = GsnTheme.typography.fontBodySmRegular,
                    color = GsnTheme.colors.textOnSolidPrimary.copy(alpha = 0.8f)
                )
            }

            // Controls
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {

                if (state.callState is CallState.IncomingRinging) {
                    // INCOMING CALL: Accept / Reject

                    // Reject (Red)
                    IconButton(
                        onClick = onEndCallClick,
                        modifier = Modifier
                            .size(36.dp)
                            .background(GsnTheme.colors.bgCriticalPrimary, CircleShape)
                    ) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.PhoneSlash,
                            contentDescription = "Reject",
                            tint = GsnTheme.colors.iconOnSolidPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Accept (Green)
                    IconButton(
                        onClick = onAcceptClick,
                        modifier = Modifier
                            .size(36.dp)
                            .background(GsnTheme.colors.bgAccentRest, CircleShape)
                    ) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.Phone,
                            contentDescription = "Accept",
                            tint = GsnTheme.colors.iconOnSolidPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                } else {
                    // ACTIVE / OUTGOING: Mute, Speaker, Hangup

                    // Mic
                    IconButton(onClick = onToggleMic, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = if (state.isMicEnabled) FontAwesomeIcons.Solid.Microphone else FontAwesomeIcons.Solid.MicrophoneSlash,
                            contentDescription = "Toggle Mic",
                            tint = GsnTheme.colors.iconOnSolidPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    // Speaker
                    IconButton(onClick = onToggleSpeaker, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = if (state.isSpeakerEnabled) FontAwesomeIcons.Solid.VolumeUp else FontAwesomeIcons.Solid.VolumeMute,
                            contentDescription = "Toggle Speaker",
                            tint = GsnTheme.colors.iconOnSolidPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    // Hangup
                    IconButton(
                        onClick = onEndCallClick,
                        modifier = Modifier
                            .size(36.dp)
                            .background(GsnTheme.colors.bgCriticalPrimary, CircleShape)
                    ) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.PhoneSlash,
                            contentDescription = "End Call",
                            tint = GsnTheme.colors.iconOnSolidPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Full Screen Call UI.
 */
@Composable
fun CallScreen(
    state: CallUiState,
    client: MatrixClient?,
    onMinimizeClick: () -> Unit,
    onEndCallClick: () -> Unit,
    onAcceptClick: () -> Unit,
    onToggleMic: () -> Unit,
    onToggleCamera: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onSwitchCamera: () -> Unit = {},
    videoContent: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Background Gradient
    val gradientColors = listOf(
        GsnTheme.colors.gradientActionStop3, // Darker Blue
        GsnTheme.colors.gradientActionStop1  // Lighter Blue
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradientColors))
    ) {
        // 1. Video Content Layer
        // Note: Use CallVideoLayout inside the passed 'videoContent' for PiP support
        if (state.isVideoCall && videoContent != null) {
            Box(modifier = Modifier.fillMaxSize().background(GsnTheme.colors.bgCanvasDefault)) {
                videoContent()
            }
        }

        // 2. Controls Layer (Overlay)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.statusBars.asPaddingValues())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Top Bar ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                IconButton(onClick = onMinimizeClick) {
                    Icon(
                        imageVector = FontAwesomeIcons.Solid.ChevronDown,
                        contentDescription = "Minimize",
                        tint = GsnTheme.colors.iconOnSolidPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                // Encryption Shield could go here
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- Center Content (Avatar) ---
            // Show avatar if it's Audio Call OR Video is disabled/loading
            val showAvatar = !(state.isVideoCall && state.isVideoEnabled && videoContent != null)

            if (showAvatar) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .background(GsnTheme.colors.bgCanvasDefault.copy(alpha = 0.2f), CircleShape)
                            .padding(20.dp)
                    ) {
                        GsnAvatarAdvanced(
                            modifier = Modifier.fillMaxSize(),
                            id = state.roomId ?: "",
                            name = state.peerName ?: "Unknown",
                            url = state.peerAvatarUrl,
                            client = client,
                            textSize = 48.sp // Larger text for initials
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = state.peerName ?: "Unknown",
                        style = GsnTheme.typography.fontHeadingXlBold,
                        color = GsnTheme.colors.textOnSolidPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Status Text with Timer
                    CallStatusText(
                        state = state,
                        style = GsnTheme.typography.fontBodyLgRegular,
                        color = GsnTheme.colors.textOnSolidPrimary.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- Bottom Controls ---
            CallControlsRow(
                state = state,
                onToggleMic = onToggleMic,
                onToggleCamera = onToggleCamera,
                onToggleSpeaker = onToggleSpeaker,
                onSwitchCamera = onSwitchCamera, // Pass down
                onEndCallClick = onEndCallClick,
                onAcceptClick = onAcceptClick
            )
        }
    }
}

@Composable
private fun CallControlsRow(
    state: CallUiState,
    onToggleMic: () -> Unit,
    onToggleCamera: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onSwitchCamera: () -> Unit,
    onEndCallClick: () -> Unit,
    onAcceptClick: () -> Unit
) {
    Surface(
        color = GsnTheme.colors.bgCanvasDefault.copy(alpha = 0.2f), // Glassmorphism-ish
        shape = RoundedCornerShape(32.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (state.callState is CallState.IncomingRinging) {
                // --- INCOMING CALL CONTROLS (Decline | Accept) ---

                Spacer(modifier = Modifier.weight(1f))

                // Decline (Red)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = onEndCallClick,
                        modifier = Modifier
                            .size(64.dp)
                            .background(GsnTheme.colors.bgCriticalPrimary, CircleShape)
                    ) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.PhoneSlash,
                            contentDescription = "Decline",
                            tint = GsnTheme.colors.iconOnSolidPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Decline", style = GsnTheme.typography.fontBodySmMedium, color = GsnTheme.colors.textOnSolidPrimary)
                }

                Spacer(modifier = Modifier.width(48.dp))

                // Accept (Green)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = onAcceptClick,
                        modifier = Modifier
                            .size(64.dp)
                            .background(GsnTheme.colors.bgAccentRest, CircleShape)
                    ) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.Phone,
                            contentDescription = "Accept",
                            tint = GsnTheme.colors.iconOnSolidPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Accept", style = GsnTheme.typography.fontBodySmMedium, color = GsnTheme.colors.textOnSolidPrimary)
                }

                Spacer(modifier = Modifier.weight(1f))

            } else {
                // --- ACTIVE / OUTGOING CONTROLS ---

                // Mic
                CallControlButton(
                    icon = if (state.isMicEnabled) FontAwesomeIcons.Solid.Microphone else FontAwesomeIcons.Solid.MicrophoneSlash,
                    label = "Mute",
                    filled = !state.isMicEnabled, // If muted, show filled background
                    onClick = onToggleMic
                )

                // Camera (Only if video call intended)
                if (state.isVideoCall) {
                    CallControlButton(
                        icon = if (state.isVideoEnabled) FontAwesomeIcons.Solid.Video else FontAwesomeIcons.Solid.VideoSlash,
                        label = "Video",
                        filled = !state.isVideoEnabled,
                        onClick = onToggleCamera
                    )

                    // Switch Camera (Only visible if camera is currently ON)
                    if (state.isVideoEnabled && state.callState is CallState.InCall) {
                        CallControlButton(
                            icon = FontAwesomeIcons.Solid.Sync,
                            label = "Flip",
                            filled = false,
                            onClick = onSwitchCamera
                        )
                    }
                }

                // Speaker
                CallControlButton(
                    icon = if (state.isSpeakerEnabled) FontAwesomeIcons.Solid.VolumeUp else FontAwesomeIcons.Solid.VolumeMute,
                    label = "Speaker",
                    filled = state.isSpeakerEnabled, // Filled when Speaker is ON
                    onClick = onToggleSpeaker
                )

                // End Call (Big Red)
                IconButton(
                    onClick = onEndCallClick,
                    modifier = Modifier
                        .size(64.dp)
                        .background(GsnTheme.colors.bgCriticalPrimary, CircleShape)
                ) {
                    Icon(
                        imageVector = FontAwesomeIcons.Solid.PhoneSlash,
                        contentDescription = "End Call",
                        tint = GsnTheme.colors.iconOnSolidPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CallControlButton(
    icon: ImageVector,
    label: String,
    filled: Boolean,
    onClick: () -> Unit
) {
    // Filled: White bg, Black icon
    // Outline/Normal: Transparent bg, White icon
    val backgroundColor = if (filled) GsnTheme.colors.bgCanvasDefault else Color.Transparent
    val iconColor = if (filled) GsnTheme.colors.iconPrimary else GsnTheme.colors.iconOnSolidPrimary

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(backgroundColor)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, style = GsnTheme.typography.fontBodyXsRegular, color = GsnTheme.colors.textOnSolidPrimary)
    }
}


/**
 * Renders the textual status of the call (e.g., "Connecting...", "02:34", "Call Ended")
 */
@OptIn(ExperimentalTime::class)
@Composable
private fun CallStatusText(
    state: CallUiState,
    style: androidx.compose.ui.text.TextStyle,
    color: Color
) {
    val text = when (val s = state.callState) {
        is CallState.Idle -> "Idle"
        is CallState.OutgoingRinging -> "Calling..."
        is CallState.IncomingRinging -> {
            if (state.isVideoCall) {
                "Incoming video call..."
            } else {
                "Incoming call..."
            }
        }
        is CallState.Connecting -> "Connecting..."
        is CallState.InCall -> {
            // Calculate duration
            var durationSeconds by remember(s.startTimestamp) { mutableStateOf(0L) }

            LaunchedEffect(s.startTimestamp) {
                while (isActive) {
                    val now = Clock.System.now().toEpochMilliseconds()
                    durationSeconds = (now - s.startTimestamp) / 1000
                    delay(1000)
                }
            }
            formatDuration(durationSeconds)
        }
        is CallState.Ended -> {
            when (s.reason) {
                EndCallReason.LocalHangup -> "Call Ended"
                EndCallReason.RemoteHangup -> "Call Ended"
                EndCallReason.Rejected -> "Call Rejected"
                EndCallReason.Busy -> "User Busy"
                EndCallReason.IceFailed -> "Connection Failed"
                EndCallReason.ConnectionDropped -> "Connection Lost"
                EndCallReason.Error -> "Call Error"
            }
        }
    }

    Text(
        text = text,
        style = style,
        color = color
    )
}

private fun formatDuration(seconds: Long): String {
    val m = seconds / 60
    val s = seconds % 60
    return "${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
}


@Preview
@Composable
private fun CallScreenPreview_Outgoing() {
    val sampleState = remember {
        CallUiState(
            roomId = "1",
            peerId = "@alice:matrix.org",
            peerName = "Alice Wonder",
            isVideoCall = true,
            isVideoEnabled = false,
            isMicEnabled = true,
            isSpeakerEnabled = true,
            callState = CallState.OutgoingRinging("1", "2")
        )
    }
    GsnPreview {
        CallScreen(
            state = sampleState,
            client = null,
            onMinimizeClick = {},
            onEndCallClick = {},
            onAcceptClick = {},
            onToggleMic = {},
            onToggleCamera = {},
            onToggleSpeaker = {}
        )
    }
}

@Preview
@Composable
private fun CallScreenPreview_Incoming() {
    val sampleState = remember {
        CallUiState(
            roomId = "1",
            peerId = "@bob:matrix.org",
            peerName = "Bob Builder",
            isVideoCall = true,
            isVideoEnabled = false,
            isMicEnabled = true,
            isSpeakerEnabled = true,
            callState = CallState.IncomingRinging(
                callId = "1",
                opponentId = "2",
                offerSdp = ""
            )
        )
    }
    GsnPreview {
        CallScreen(
            state = sampleState,
            client = null,
            onMinimizeClick = {},
            onEndCallClick = {},
            onAcceptClick = {},
            onToggleMic = {},
            onToggleCamera = {},
            onToggleSpeaker = {}
        )
    }
}

@Preview
@Composable
private fun CallBannerPreview() {
    val sampleState = remember {
        CallUiState(
            roomId = "1",
            peerId = "2",
            peerName = "Bob",
            callState = CallState.InCall("1", "2", 1715000000000L)
        )
    }
    GsnPreview {
        CallOverlayBanner(
            state = sampleState,
            client = null,
            onExpandClick = {},
            onEndCallClick = {},
            onAcceptClick = {},
            onToggleMic = {},
            onToggleSpeaker = {}
        )
    }
}
