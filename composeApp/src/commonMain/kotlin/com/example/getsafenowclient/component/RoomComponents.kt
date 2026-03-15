package com.example.getsafenowclient.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.getsafenowclient.call.CallBubbleType
import com.example.getsafenowclient.common.ui.GsnAvatarAdvanced
import com.example.getsafenowclient.ui.tokens.DesignTokens
import com.example.getsafenowclient.utils.DurationFormatter
import com.example.getsafenowclient.utils.fullDayText
import com.example.getsafenowclient.utils.timeText
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Regular
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.regular.Star
import compose.icons.fontawesomeicons.solid.ArrowCircleRight
import compose.icons.fontawesomeicons.solid.ArrowLeft
import compose.icons.fontawesomeicons.solid.Image
import compose.icons.fontawesomeicons.solid.Microphone
import compose.icons.fontawesomeicons.solid.Paperclip
import compose.icons.fontawesomeicons.solid.Phone
import compose.icons.fontawesomeicons.solid.PhoneSlash
import compose.icons.fontawesomeicons.solid.Plus
import compose.icons.fontawesomeicons.solid.UserPlus
import compose.icons.fontawesomeicons.solid.Video
import compose.icons.fontawesomeicons.solid.VideoSlash
import io.getsafenow.libraries.designcomponents.utils.preview.GsnPreview
import io.getsafenow.libraries.gsn_theme.customtheme.GsnTheme
import net.folivo.trixnity.client.MatrixClient
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * The top app bar for a room, displaying avatar, name, status, and actions.
 */
@Composable
fun RoomHeader(
    modifier: Modifier = Modifier,
    client: MatrixClient?,
    roomId: String,
    roomName: String,
    roomAvatarUrl: String?,
    roomStatus: String, // e.g., "Active now"
    statusColor: Color = GsnTheme.colors.textSecondary,
    onBackClick: () -> Unit,
    onStarClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(GsnTheme.colors.bgCanvasDefault)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            .padding(horizontal = DesignTokens.Spacing.sm, vertical = DesignTokens.Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md)
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = FontAwesomeIcons.Solid.ArrowLeft,
                contentDescription = "Back",
                tint = GsnTheme.colors.iconPrimary,
                modifier = Modifier.size(DesignTokens.IconSize.medium)
            )
        }
        GsnAvatarAdvanced(
            modifier = Modifier.size(DesignTokens.AvatarSize.medium),
            id = roomId,
            name = roomName,
            url = roomAvatarUrl,
            client = client
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = roomName,
                style = GsnTheme.typography.fontBodyLgMedium,
                color = GsnTheme.colors.textPrimary
            )
            Text(
                text = roomStatus,
                style = GsnTheme.typography.fontBodySmMedium,
                color = statusColor
            )
        }
        IconButton(onClick = onStarClick) {
            Icon(
                imageVector = FontAwesomeIcons.Regular.Star,
                contentDescription = "Favorite",
                tint = GsnTheme.colors.iconSecondary,
                modifier = Modifier.size(DesignTokens.IconSize.medium)
            )
        }
    }
}

/**
 * A single chat message bubble, styled for incoming or outgoing messages.
 */
@OptIn(ExperimentalTime::class)
@Composable
fun ChatMessageBubble(
    text: String,
    timestamp: Long,
    isMine: Boolean,
    modifier: Modifier = Modifier,
    isSending: Boolean = false,
    isError: Boolean = false,
) {
    val bubbleColor = if (isMine) GsnTheme.colors.bgAccentRest else GsnTheme.colors.bgSubtleSecondary
    val textColor = if (isMine) GsnTheme.colors.textOnSolidPrimary else GsnTheme.colors.textPrimary
    val horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = horizontalArrangement
    ) {
        Column(
            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
            modifier = Modifier
                .fillMaxWidth(DesignTokens.ContentWidth.messageBubbleFraction)
                .widthIn(max = DesignTokens.ContentWidth.messageBubbleMax)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(DesignTokens.CornerRadius.md))
                    .background(bubbleColor)
                    .padding(horizontal = DesignTokens.Spacing.md, vertical = DesignTokens.Spacing.sm)
            ) {
                Text(
                    text = text,
                    style = GsnTheme.typography.fontBodyLgMedium,
                    color = textColor
                )
            }
            // ✅ Dynamically show the status (time, sending, or error)
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
                modifier = Modifier.padding(horizontal = DesignTokens.Spacing.xs, vertical = DesignTokens.Spacing.xs / 2)
            )
        }
    }
}

/**
 * A specialised bubble for call events (Voice/Video started, ended, missed).
 */
@OptIn(ExperimentalTime::class)
@Composable
fun VoiceCallBubble(
    type: CallBubbleType,
    isMine: Boolean,
    isVideo: Boolean,
    timestamp: Long,
    durationMs: Long? = null,
    modifier: Modifier = Modifier
) {
    val bubbleColor =
        if (isMine) GsnTheme.colors.bgAccentRest else GsnTheme.colors.bgSubtleSecondary
    val contentColor =
        if (isMine) GsnTheme.colors.textOnSolidPrimary else GsnTheme.colors.textPrimary
    val horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start

    // Choose icon based on type + media
    val icon = when (type) {
        CallBubbleType.INCOMING_MISSED,
        CallBubbleType.OUTGOING_MISSED,
        CallBubbleType.FAILED -> {
            if (isVideo) FontAwesomeIcons.Solid.VideoSlash else FontAwesomeIcons.Solid.PhoneSlash
        }

        else -> {
            if (isVideo) FontAwesomeIcons.Solid.Video else FontAwesomeIcons.Solid.Phone
        }
    }

    // Main title text (top line)
    val title = when (type) {
        CallBubbleType.OUTGOING_RINGING ->
            if (isVideo) "Outgoing video call" else "Outgoing voice call"

        CallBubbleType.OUTGOING_CANCELLED ->
            "Cancelled call"

        CallBubbleType.OUTGOING_MISSED ->
            "Missed call"

        CallBubbleType.OUTGOING_ENDED ->
            if (isVideo) "Video call" else "Voice call"

        CallBubbleType.INCOMING_RINGING ->
            if (isVideo) "Incoming video call" else "Incoming call"

        CallBubbleType.INCOMING_MISSED ->
            "Missed call"

        CallBubbleType.INCOMING_DECLINED ->
            "Declined call"

        CallBubbleType.INCOMING_ENDED ->
            if (isVideo) "Video call" else "Voice call"

        CallBubbleType.FAILED ->
            "Call failed"
    }

    // Secondary line under title
    val subtitle: String? = when {
        durationMs != null && type in setOf(
            CallBubbleType.OUTGOING_ENDED,
            CallBubbleType.INCOMING_ENDED
        ) -> {
            "Duration ${DurationFormatter.formatMillis(durationMs)}"
        }

        type == CallBubbleType.OUTGOING_RINGING || type == CallBubbleType.INCOMING_RINGING ->
            "Ringing..."

        type == CallBubbleType.INCOMING_MISSED || type == CallBubbleType.OUTGOING_MISSED ->
            "Tap to call back"

        type == CallBubbleType.FAILED ->
            "Connection problem"

        else -> null
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = horizontalArrangement
    ) {
        Column(
            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
            modifier = Modifier
                .fillMaxWidth(DesignTokens.ContentWidth.messageBubbleFraction)
                .widthIn(max = DesignTokens.ContentWidth.messageBubbleMax)
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(DesignTokens.CornerRadius.md))
                    .background(bubbleColor)
                    .padding(horizontal = DesignTokens.Spacing.md, vertical = DesignTokens.Spacing.sm + DesignTokens.Spacing.xs / 2),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm)
            ) {
                // Icon container
                Box(
                    modifier = Modifier
                        .size(DesignTokens.IconSize.large)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(DesignTokens.IconSize.small)
                    )
                }

                Column {
                    Text(
                        text = title,
                        style = GsnTheme.typography.fontBodyLgMedium,
                        color = contentColor
                    )
                    subtitle?.let {
                        Text(
                            text = it,
                            style = GsnTheme.typography.fontBodySmRegular,
                            color = contentColor.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Time below bubble
            Text(
                text = Instant.fromEpochMilliseconds(timestamp).timeText(),
                style = GsnTheme.typography.fontBodySmMedium,
                color = GsnTheme.colors.textSecondary,
                modifier = Modifier.padding(horizontal = DesignTokens.Spacing.xs, vertical = DesignTokens.Spacing.xs / 2)
            )
        }
    }
}

/**
 * A date separator for the chat timeline.
 */
@OptIn(ExperimentalTime::class)
@Composable
fun DateSeparator(
    timestamp: Long,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth().padding(vertical = DesignTokens.Spacing.lg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = Instant.fromEpochMilliseconds(timestamp).fullDayText(),
            style = GsnTheme.typography.fontBodySmMedium,
            color = GsnTheme.colors.textSecondary,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(GsnTheme.colors.bgSubtleSecondary)
                .padding(vertical = DesignTokens.Spacing.xs, horizontal = DesignTokens.Spacing.md)
        )
    }
}

/**
 * The message input field at the bottom of the screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onStartRecording: () -> Unit, // Voice
    onStartVideoRecording: () -> Unit, // Video
    onSendImage: () -> Unit,
    onSendFile: () -> Unit,
    onStartVoiceCall: () -> Unit,
    onStartVideoCall: () -> Unit,
    onAddPeople: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // State to toggle between Voice and Video mode
    var isVoiceMode by remember { mutableStateOf(true) }
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(GsnTheme.colors.bgCanvasDefault)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
            .padding(horizontal = DesignTokens.Spacing.lg, vertical = DesignTokens.Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm)
    ) {
        // 1. Left: Text Input
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            textStyle = GsnTheme.typography.fontBodyMdRegular.copy(color = GsnTheme.colors.textPrimary),
            cursorBrush = SolidColor(GsnTheme.colors.iconAccentPrimary),
            decorationBox = { innerTextField ->
                TextFieldDefaults.DecorationBox(
                    value = value,
                    innerTextField = innerTextField,
                    enabled = true,
                    singleLine = false,
                    visualTransformation = VisualTransformation.None,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    placeholder = { Text("Type a message...", style = GsnTheme.typography.fontBodyMdRegular, color = GsnTheme.colors.textSecondary) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = GsnTheme.colors.bgSubtleSecondary,
                        unfocusedContainerColor = GsnTheme.colors.bgSubtleSecondary,
                        disabledContainerColor = GsnTheme.colors.bgSubtleSecondary,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                    ),
                    contentPadding = PaddingValues(horizontal = DesignTokens.Spacing.lg, vertical = DesignTokens.Spacing.sm + DesignTokens.Spacing.xs / 2),
                    shape = RoundedCornerShape(DesignTokens.CornerRadius.md + DesignTokens.CornerRadius.sm),
                )
            }
        )

        // 2. Middle: Send Button
        IconButton(
            onClick = onSendClick,
            enabled = value.isNotBlank(),
            modifier = Modifier.size(DesignTokens.IconSize.large + DesignTokens.Spacing.xs)
        ) {
            Icon(
                imageVector = FontAwesomeIcons.Solid.ArrowCircleRight,
                contentDescription = "Send",
                tint = if (value.isNotBlank()) GsnTheme.colors.bgAccentRest else GsnTheme.colors.iconDisabled,
                modifier = Modifier.size(DesignTokens.IconSize.mediumSmall + DesignTokens.Spacing.xs)
            )
        }

        // 3. Middle-Right: More Options Button (Square)
        Box {
            Box(
                modifier = Modifier
                    .size(DesignTokens.IconSize.large + DesignTokens.Spacing.xs)
                    .clip(RoundedCornerShape(DesignTokens.CornerRadius.sm))
                    .background(GsnTheme.colors.bgSubtleSecondary)
                    .clickable { showMenu = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = FontAwesomeIcons.Solid.Plus,
                    contentDescription = "More options",
                    tint = GsnTheme.colors.iconPrimary,
                    modifier = Modifier.size(DesignTokens.IconSize.mediumSmall)
                )
            }

            // Popup Menu
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(GsnTheme.colors.bgCanvasDefault)
            ) {
                DropdownMenuItem(
                    text = { Text("Send Image", style = GsnTheme.typography.fontBodyMdRegular, color = GsnTheme.colors.textPrimary) },
                    onClick = { showMenu = false; onSendImage() },
                    leadingIcon = { Icon(FontAwesomeIcons.Solid.Image, contentDescription = null, tint = GsnTheme.colors.iconSecondary, modifier = Modifier.size(DesignTokens.IconSize.mediumSmall)) },
                    colors = MenuDefaults.itemColors(
                        textColor = GsnTheme.colors.textPrimary,
                        leadingIconColor = GsnTheme.colors.iconSecondary
                    )
                )
                DropdownMenuItem(
                    text = { Text("Send File", style = GsnTheme.typography.fontBodyMdRegular, color = GsnTheme.colors.textPrimary) },
                    onClick = { showMenu = false; onSendFile() },
                    leadingIcon = { Icon(FontAwesomeIcons.Solid.Paperclip, contentDescription = null, tint = GsnTheme.colors.iconSecondary, modifier = Modifier.size(20.dp)) },
                    colors = MenuDefaults.itemColors(
                        textColor = GsnTheme.colors.textPrimary,
                        leadingIconColor = GsnTheme.colors.iconSecondary
                    )
                )
                DropdownMenuItem(
                    text = { Text("Start Voice Call", style = GsnTheme.typography.fontBodyMdRegular, color = GsnTheme.colors.textPrimary) },
                    onClick = { showMenu = false; onStartVoiceCall() },
                    leadingIcon = { Icon(FontAwesomeIcons.Solid.Phone, contentDescription = null, tint = GsnTheme.colors.iconSecondary, modifier = Modifier.size(20.dp)) },
                    colors = MenuDefaults.itemColors(
                        textColor = GsnTheme.colors.textPrimary,
                        leadingIconColor = GsnTheme.colors.iconSecondary
                    )
                )
                DropdownMenuItem(
                    text = { Text("Start Video Call", style = GsnTheme.typography.fontBodyMdRegular, color = GsnTheme.colors.textPrimary) },
                    onClick = { showMenu = false; onStartVideoCall() },
                    leadingIcon = { Icon(FontAwesomeIcons.Solid.Video, contentDescription = null, tint = GsnTheme.colors.iconSecondary, modifier = Modifier.size(20.dp)) },
                    colors = MenuDefaults.itemColors(
                        textColor = GsnTheme.colors.textPrimary,
                        leadingIconColor = GsnTheme.colors.iconSecondary
                    )
                )
                DropdownMenuItem(
                    text = { Text("Add People to Chat", style = GsnTheme.typography.fontBodyMdRegular, color = GsnTheme.colors.textPrimary) },
                    onClick = { showMenu = false; onAddPeople() },
                    leadingIcon = { Icon(FontAwesomeIcons.Solid.UserPlus, contentDescription = null, tint = GsnTheme.colors.iconSecondary, modifier = Modifier.size(20.dp)) },
                    colors = MenuDefaults.itemColors(
                        textColor = GsnTheme.colors.textPrimary,
                        leadingIconColor = GsnTheme.colors.iconSecondary
                    )
                )
            }
        }

        // 4. Right: Media Toggle (Voice/Video)
        // Tap -> Swap Icon
        // Long Press -> Trigger Action
        val mediaIcon = if (isVoiceMode) FontAwesomeIcons.Solid.Microphone else FontAwesomeIcons.Solid.Video
        val contentDesc = if (isVoiceMode) "Voice Message" else "Video Message"

        Box(
            modifier = Modifier
                .size(DesignTokens.AvatarSize.medium)
                .clip(RoundedCornerShape(DesignTokens.CornerRadius.md + DesignTokens.Spacing.sm))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            isVoiceMode = !isVoiceMode
                        },
                        onLongPress = {
                            if (isVoiceMode) {
                                onStartRecording()
                            } else {
                                onStartVideoRecording()
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = mediaIcon,
                contentDescription = contentDesc,
                tint = GsnTheme.colors.iconDisabled,
                modifier = Modifier.size(DesignTokens.IconSize.mediumSmall + DesignTokens.Spacing.xs / 2)
            )
        }
    }
}


@Composable
fun ChatSystemMessageBubble(text: String, timestamp: Long) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = DesignTokens.Spacing.xs),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = GsnTheme.typography.fontBodySmRegular,
            color = GsnTheme.colors.textActionAccent
        )
    }
}

// ----------------------------------------------------------------------
// PREVIEWS
// ----------------------------------------------------------------------

@Preview
@Composable
private fun RoomHeaderPreview() {
    GsnPreview {
        RoomHeader(
            client = null,
            roomId = "dummy",
            roomName = "Sarah Wilson",
            roomAvatarUrl = null,
            roomStatus = "Active now",
            onBackClick = {},
            onStarClick = {}
        )
    }
}

@OptIn(ExperimentalTime::class)
@Preview
@Composable
private fun ChatMessageBubblePreview() {
    val now = Clock.System.now().toEpochMilliseconds()
    GsnPreview {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ChatMessageBubble(
                text = "Incoming message from Sarah",
                timestamp = now,
                isMine = false
            )
            ChatMessageBubble(
                text = "My message",
                timestamp = now,
                isMine = true
            )
            ChatMessageBubble(
                text = "Sending...",
                timestamp = now,
                isMine = true,
                isSending = true
            )
            ChatMessageBubble(
                text = "Failed to send",
                timestamp = now,
                isMine = true,
                isError = true
            )
        }
    }
}

@OptIn(ExperimentalTime::class)
@Preview
@Composable
private fun VoiceCallBubblePreview() {
    val now = Clock.System.now().toEpochMilliseconds()
    GsnPreview {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Outgoing ended voice
            VoiceCallBubble(
                type = CallBubbleType.OUTGOING_ENDED,
                isMine = true,
                isVideo = false,
                durationMs = 125_000,
                timestamp = now
            )

            // Incoming missed video
            VoiceCallBubble(
                type = CallBubbleType.INCOMING_MISSED,
                isMine = false,
                isVideo = true,
                timestamp = now
            )

            // Ringing incoming
            VoiceCallBubble(
                type = CallBubbleType.INCOMING_RINGING,
                isMine = false,
                isVideo = false,
                timestamp = now
            )

            // Failed call
            VoiceCallBubble(
                type = CallBubbleType.FAILED,
                isMine = true,
                isVideo = true,
                timestamp = now
            )
        }
    }
}


@OptIn(ExperimentalTime::class)
@Preview
@Composable
private fun SystemAndDatePreview() {
    val now = Clock.System.now().toEpochMilliseconds()
    GsnPreview {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DateSeparator(timestamp = now)
            ChatSystemMessageBubble(text = "Sarah Wilson joined the chat", timestamp = now)
        }
    }
}

@Preview
@Composable
private fun MessageInputPreview() {
    GsnPreview {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Empty State
            MessageInput(
                value = "",
                onValueChange = {},
                onSendClick = {},
                onStartRecording = {},
                onStartVideoRecording = {},
                onSendImage = {},
                onSendFile = {},
                onStartVoiceCall = {},
                onStartVideoCall = {},
                onAddPeople = {}
            )
            // Typed State
            MessageInput(
                value = "Hello!",
                onValueChange = {},
                onSendClick = {},
                onStartRecording = {},
                onStartVideoRecording = {},
                onSendImage = {},
                onSendFile = {},
                onStartVoiceCall = {},
                onStartVideoCall = {},
                onAddPeople = {}
            )
        }
    }
}

@OptIn(ExperimentalTime::class)
@Preview
@Composable
private fun FullRoomScreenPreview() {
    val now = Clock.System.now()
    var message by remember { mutableStateOf("") }

    GsnPreview {
        Column(modifier = Modifier.fillMaxWidth().background(GsnTheme.colors.bgCanvasDefault)) {
            RoomHeader(
                client = null,
                roomId = "!123:example.com",
                roomName = "Sarah Wilson",
                roomAvatarUrl = null,
                roomStatus = "Active now",
                onBackClick = {},
                onStarClick = {}
            )
            
            // Mock Timeline
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(Modifier.height(16.dp))
                DateSeparator(timestamp = now.toEpochMilliseconds())
                
                ChatMessageBubble(
                    text = "Hey! How are you doing?",
                    timestamp = (now - 5.minutes).toEpochMilliseconds(),
                    isMine = false
                )
                ChatMessageBubble(
                    text = "I'm doing great! Just finished the project.",
                    timestamp = (now - 4.minutes).toEpochMilliseconds(),
                    isMine = true
                )
                ChatSystemMessageBubble(
                    text = "You missed a call from Sarah",
                    timestamp = (now - 3.minutes).toEpochMilliseconds()
                )

// ----------------------------------------------------------
                // UPDATED CALL BUBBLES (new VoiceCallBubble API)
                // ----------------------------------------------------------

                // Outgoing ended voice call (with duration)
                VoiceCallBubble(
                    type = CallBubbleType.OUTGOING_ENDED,
                    isMine = true,
                    isVideo = false,
                    durationMs = 125_000, // 2 minutes 5 sec
                    timestamp = (now - 2.minutes).toEpochMilliseconds()
                )

                // Incoming missed video call
                VoiceCallBubble(
                    type = CallBubbleType.INCOMING_MISSED,
                    isMine = false,
                    isVideo = true,
                    timestamp = (now - 1.minutes).toEpochMilliseconds()
                )

                // Incoming ringing voice call
                VoiceCallBubble(
                    type = CallBubbleType.INCOMING_RINGING,
                    isMine = false,
                    isVideo = false,
                    timestamp = now.toEpochMilliseconds()
                )
            }
            
            MessageInput(
                value = message,
                onValueChange = { message = it },
                onSendClick = { message = "" },
                onStartRecording = {},
                onStartVideoRecording = {},
                onSendImage = {},
                onSendFile = {},
                onStartVoiceCall = {},
                onStartVideoCall = {},
                onAddPeople = {}
            )
        }
    }
}

