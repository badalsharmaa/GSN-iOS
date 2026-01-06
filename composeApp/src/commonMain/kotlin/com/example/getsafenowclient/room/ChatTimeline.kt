package com.example.getsafenowclient.room

import com.example.getsafenowclient.call.CallScreenModel
import com.example.getsafenowclient.permissions.CameraPermission
import com.example.getsafenowclient.permissions.MicrophonePermission
import com.example.getsafenowclient.room.sharing.VoiceMessageEvent
import com.example.getsafenowclient.room.sharing.VoiceRecorderUiState
import io.getsafenow.libraries.architecture.ScreenComponent
import io.getsafenow.libraries.kmputils.platformkmp.ContextFactory
import io.getsafenow.libraries.kmputils.platformkmp.PlatformFile
import kotlinx.coroutines.flow.StateFlow
import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent

// -------------------------------------------------------------
// Public API for Room ViewModel
// -------------------------------------------------------------
interface ChatTimeline : ScreenComponent {
    val items: StateFlow<List<UITimelineItem>>
    val isLoadingBefore: StateFlow<Boolean>
    val isLoadingAfter: StateFlow<Boolean>
    val isInitialLoadComplete: StateFlow<Boolean>

    // Exposed for VideoPresenter dependencies
    val contextFactory: ContextFactory
    val cameraPermission: CameraPermission
    val microphonePermission: MicrophonePermission

    // Voice Recorder State
    val voiceRecorderState: StateFlow<VoiceRecorderUiState>

    // Video Player State
    val playingVideoUrl: StateFlow<String?>
    
    // Call Feature
    val callModel: CallScreenModel

    suspend fun loadBefore()
    suspend fun loadAfter()
    suspend fun sendMessage(text: String)
    suspend fun sendVideoMessage(file: PlatformFile, duration: Long, thumbnail: PlatformFile?)

    // Voice Recorder Events
    fun onVoiceMessageEvent(event: VoiceMessageEvent)

    // Audio Playback
    fun playVoiceMessage(eventId: String, content: RoomMessageEventContent.FileBased.Audio)
    fun pauseVoiceMessage()
    val currentlyPlayingEventId: StateFlow<String?>
    val currentPlaybackPosition: StateFlow<Long>
    val isPlaying: StateFlow<Boolean>

    // Video Playback
    fun onPlayVideo(content: RoomMessageEventContent.FileBased.Video)
    fun onDismissVideoPlayer()
    fun onCallBubbleClicked(
        callId: String,
        isVideo: Boolean,
        isIncoming: Boolean
    )
}
