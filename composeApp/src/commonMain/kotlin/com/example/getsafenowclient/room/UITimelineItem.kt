package com.example.getsafenowclient.room

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.getsafenowclient.call.CallBubbleType
import com.example.getsafenowclient.common.events.message.MessageEventFormatter
import com.example.getsafenowclient.common.ui.ThumbnailLoader
import com.example.getsafenowclient.component.ChatMessageBubble
import com.example.getsafenowclient.component.ChatSystemMessageBubble
import com.example.getsafenowclient.component.DateSeparator
import com.example.getsafenowclient.component.VoiceCallBubble
import com.example.getsafenowclient.component.chat.VideoMessageBubble
import com.example.getsafenowclient.component.chat.VoiceMessageBubble
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.client.store.RoomUser
import net.folivo.trixnity.client.store.TimelineEvent
import net.folivo.trixnity.client.store.eventId
import net.folivo.trixnity.client.store.isEncrypted
import net.folivo.trixnity.core.model.UserId
import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent
import kotlin.time.ExperimentalTime

interface UITimelineItem {
    val id: String
    val timestamp: Long

    @Composable
    fun render(
        modifier: Modifier,
        client: MatrixClient,
        component: ChatTimeline,
        isFirstInBlock: Boolean,
        isLastInBlock: Boolean
    )
}

data class MessageItem(
    val event: TimelineEvent,
    val senderId: UserId,
    val senderState: StateFlow<RoomUser?>,
) : UITimelineItem {

    override val id = event.eventId.full
    override val timestamp: Long = event.event.originTimestamp

    @OptIn(ExperimentalTime::class)
    @Composable
    override fun render(
        modifier: Modifier,
        client: MatrixClient,
        component: ChatTimeline,
        isFirstInBlock: Boolean,
        isLastInBlock: Boolean
    ) {
        val isMyMessage = senderId == client.userId
        val decrypted = event.content?.getOrNull()

        if (decrypted is RoomMessageEventContent.FileBased.Audio) {
            // Voice Message (Audio)
            val durationMs = decrypted.info?.duration?.toLong() ?: 0L

            val currentlyPlayingId by component.currentlyPlayingEventId.collectAsState()
            val isPlayingGlobal by component.isPlaying.collectAsState()
            val currentPositionGlobal by component.currentPlaybackPosition.collectAsState()

            val isThisPlaying = currentlyPlayingId == id
            val isPlaying = isThisPlaying && isPlayingGlobal
            val currentPositionMs = if (isThisPlaying) currentPositionGlobal else 0L

            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = if (isMyMessage) Arrangement.End else Arrangement.Start
            ) {
                VoiceMessageBubble(
                    modifier = Modifier,
                    durationMs = durationMs,
                    currentPositionMs = currentPositionMs,
                    isPlaying = isPlaying,
                    isMine = isMyMessage,
                    onPlayPauseClick = {
                        component.playVoiceMessage(id, decrypted)
                    },
                    timestamp = timestamp
                )
            }
        } else if (decrypted is RoomMessageEventContent.FileBased.Video) {
            // Video Message
            val durationMs = decrypted.info?.duration?.toLong() ?: 0L
            val durationStr = formatDuration(durationMs)
            val thumbMxcUrl = decrypted.info?.thumbnailUrl
           // val httpThumbUrl = thumbMxcUrl?.let { client.media.getDownloadUrl(it) }

            if (thumbMxcUrl != null) {
                VideoMessageBubble(
                    modifier = modifier,
                    isMine = isMyMessage,
                    duration = durationStr,
                    timestamp = timestamp,
                    thumbnail = {
                        ThumbnailLoader(
                            client = client,
                            mxcUrl = thumbMxcUrl
                        )
                    },
                    onPlayClick = { component.onPlayVideo(decrypted) }
                )
            } else {
                VideoMessageBubble(
                    modifier = modifier,
                    isMine = isMyMessage,
                    duration = durationStr,
                    timestamp = timestamp,
                    onPlayClick = { component.onPlayVideo(decrypted) }
                )
            }
        } else {
            // Default Text / generic fallback
            val messageText =
                if (decrypted is RoomMessageEventContent) {
                    MessageEventFormatter.formatTimeline(decrypted)
                } else {
                    if (event.isEncrypted) "🔒 [encrypted]" else "[unknown]"
                }

            ChatMessageBubble(
                text = messageText,
                timestamp = timestamp,
                isMine = isMyMessage,
                modifier = modifier
            )
        }
    }
}


data class StateItem(
    val event: TimelineEvent,
    val senderState: StateFlow<RoomUser?>
) : UITimelineItem {

    override val id = event.eventId.full
    override val timestamp: Long = event.event.originTimestamp

    @Composable
    override fun render(
        modifier: Modifier,
        client: MatrixClient,
        component: ChatTimeline,
        isFirstInBlock: Boolean,
        isLastInBlock: Boolean
    ) {
        DateSeparator(timestamp = timestamp, modifier = modifier)
    }
}

data class DateItem(
    override val timestamp: Long
) : UITimelineItem {

    override val id = timestamp.toString()

    @Composable
    override fun render(
        modifier: Modifier,
        client: MatrixClient,
        component: ChatTimeline,
        isFirstInBlock: Boolean,
        isLastInBlock: Boolean
    ) {
        DateSeparator(timestamp = timestamp, modifier = modifier)
    }
}

// ------------------------------------------------------------------
// CALL ITEM
// ------------------------------------------------------------------
// ------------------------------------------------------------------
// CALL ITEM
// ------------------------------------------------------------------
data class CallItem(
    override val id: String,
    override val timestamp: Long,
    val senderId: UserId,          // The user who initiated the call
    val type: CallBubbleType,
    val isVideo: Boolean,
    val durationMs: Long?,
    val isMissed: Boolean
) : UITimelineItem {

    @Composable
    override fun render(
        modifier: Modifier,
        client: MatrixClient,
        component: ChatTimeline,
        isFirstInBlock: Boolean,
        isLastInBlock: Boolean
    ) {
        val isMyMessage = senderId == client.userId

        VoiceCallBubble(
            modifier = modifier
                .clickable {
                    component.onCallBubbleClicked(
                        callId = id,
                        isVideo = isVideo,
                        isIncoming = !isMyMessage
                    )
                },
            type = type,
            isMine = isMyMessage,
            isVideo = isVideo,
            timestamp = timestamp,
            durationMs = durationMs
        )
    }
}

// ------------------------------------------------------------------
// EMPTY ITEM (to hide certain events)
// ------------------------------------------------------------------
data class EmptyItem(override val id: String, override val timestamp: Long) : UITimelineItem {
    @Composable
    override fun render(
        modifier: Modifier,
        client: MatrixClient,
        component: ChatTimeline,
        isFirstInBlock: Boolean,
        isLastInBlock: Boolean
    ) {
        // Render Nothing
    }
}

// ------------------------------------------------------------------
// OUTBOX MESSAGE (pending send)
// ------------------------------------------------------------------
data class OutboxItem(
    override val id: String,          // transactionId
    val content: RoomMessageEventContent?, // Added content
    val text: String,                 // pending message text
    override val timestamp: Long,
    val isMine: Boolean = true,
    val isError: Boolean = false,
    val isSent: Boolean = false 
) : UITimelineItem {

    @OptIn(ExperimentalTime::class)
    @Composable
    override fun render(
        modifier: Modifier,
        client: MatrixClient,
        component: ChatTimeline,
        isFirstInBlock: Boolean,
        isLastInBlock: Boolean
    ) {
        val scope = androidx.compose.runtime.rememberCoroutineScope()
        
        if (content is RoomMessageEventContent.FileBased.Audio) {
            val durationMs = content.info?.duration?.toLong() ?: 0L

            val currentlyPlayingId by component.currentlyPlayingEventId.collectAsState()
            val isPlayingGlobal by component.isPlaying.collectAsState()
            val currentPositionGlobal by component.currentPlaybackPosition.collectAsState()

            val isThisPlaying = currentlyPlayingId == id
            val isPlaying = isThisPlaying && isPlayingGlobal
            val currentPositionMs = if (isThisPlaying) currentPositionGlobal else 0L

            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                VoiceMessageBubble(
                    modifier = Modifier.clickable(enabled = isError) {
                         scope.launch { component.retryMessage(id) }
                    },
                    durationMs = durationMs,
                    currentPositionMs = currentPositionMs,
                    isPlaying = isPlaying,
                    isMine = isMine,
                    onPlayPauseClick = {
                        component.playVoiceMessage(id, content)
                    },
                    timestamp = timestamp,
                    isSending = !isSent && !isError,
                    isError = isError
                )
            }
        } else if (content is RoomMessageEventContent.FileBased.Video) {
            // Video Outbox
            val durationMs = content.info?.duration?.toLong() ?: 0L
            val durationStr = formatDuration(durationMs)
            val thumbMxcUrl = content.info?.thumbnailUrl
         //   val httpThumbUrl = thumbMxcUrl?.let { client.media.getDownloadUrl(it) }

            if (thumbMxcUrl != null) {
                VideoMessageBubble(
                    modifier = modifier.clickable(enabled = isError) {
                         scope.launch { component.retryMessage(id) }
                    },
                    isMine = isMine,
                    duration = durationStr,
                    timestamp = timestamp,
                    isSending = !isSent && !isError,
                    isError = isError,
                    thumbnail = {
                        ThumbnailLoader(
                            client = client,
                            mxcUrl = thumbMxcUrl
                        )
                    },
                    onPlayClick = { component.onPlayVideo(content) }
                )
            } else {
                VideoMessageBubble(
                    modifier = modifier.clickable(enabled = isError) {
                         scope.launch { component.retryMessage(id) }
                    },
                    isMine = isMine,
                    duration = durationStr,
                    timestamp = timestamp,
                    isSending = !isSent && !isError,
                    isError = isError,
                    onPlayClick = { component.onPlayVideo(content) }
                )
            }
        } else {
            ChatMessageBubble(
                text = text,
                timestamp = timestamp,
                isMine = true,
                isSending = !isSent && !isError,
                isError = isError,
                modifier = modifier.clickable(enabled = isError) {
                     scope.launch { component.retryMessage(id) }
                }
            )
        }
    }
}

// ------------------------------------------------------------------
// SYSTEM MESSAGE
// ------------------------------------------------------------------

data class SystemMessageItem(
    override val id: String,
    override val timestamp: Long,
    val text: String
) : UITimelineItem {

    @Composable
    override fun render(
        modifier: Modifier,
        client: MatrixClient,
        component: ChatTimeline,
        isFirstInBlock: Boolean,
        isLastInBlock: Boolean
    ) {
        ChatSystemMessageBubble(text = text, timestamp = timestamp)
    }
}

data class SyntheticCallItem(
    override val id: String = "synthetic_call",
    override val timestamp: Long,
    val type: CallBubbleType,
    val isVideo: Boolean
) : UITimelineItem {

    @Composable
    override fun render(
        modifier: Modifier,
        client: MatrixClient,
        component: ChatTimeline,
        isFirstInBlock: Boolean,
        isLastInBlock: Boolean
    ) {
        VoiceCallBubble(
            modifier = modifier
                .clickable {
                    component.onCallBubbleClicked(
                        callId = id,
                        isVideo = isVideo,
                        isIncoming = false
                    )
                },
            type = type,
            isMine = true,
            isVideo = isVideo,
            timestamp = timestamp,
            durationMs = null
        )
    }
}


private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return "${m}:${s.toString().padStart(2, '0')}"
}



