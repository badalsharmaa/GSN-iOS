package com.example.getsafenowclient.room

import androidx.compose.runtime.Composable
import co.touchlab.kermit.Logger
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.example.getsafenowclient.audio.AudioPlayer
import com.example.getsafenowclient.audio.AudioPlayerImpl
import com.example.getsafenowclient.audio.AudioRecorder
import com.example.getsafenowclient.audio.AudioRecorderImpl
import com.example.getsafenowclient.audio.PlayerListener
import com.example.getsafenowclient.call.CallBubbleType
import com.example.getsafenowclient.call.CallScreenModel
import com.example.getsafenowclient.call.CallState
import com.example.getsafenowclient.common.events.DefaultEventFilter
import com.example.getsafenowclient.common.events.membership.StateEventResolver
import com.example.getsafenowclient.common.events.message.MessageEventFormatter
import com.example.getsafenowclient.common.readreceipts.ReadReceiptSender
import com.example.getsafenowclient.permissions.CameraPermission
import com.example.getsafenowclient.permissions.CameraPermissionImpl
import com.example.getsafenowclient.permissions.MicrophonePermission
import com.example.getsafenowclient.permissions.MicrophonePermissionImpl
import com.example.getsafenowclient.room.sharing.VoiceMessageEvent
import com.example.getsafenowclient.room.sharing.VoiceRecorderState
import com.example.getsafenowclient.room.sharing.VoiceRecorderUiState
import com.example.getsafenowclient.service.SessionManager
import com.example.getsafenowclient.utils.CallUtils.isVideoOffer
import io.getsafenow.libraries.gsn_core.kmpmimetype.MimeTypes
import io.getsafenow.libraries.kmputils.platformkmp.ContextFactory
import io.getsafenow.libraries.kmputils.platformkmp.PlatformFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.core.model.events.m.TypingEventContent
import net.folivo.trixnity.client.media
import net.folivo.trixnity.client.room
import net.folivo.trixnity.client.store.RoomOutboxMessage
import net.folivo.trixnity.client.store.RoomUser
import net.folivo.trixnity.client.store.TimelineEvent
import net.folivo.trixnity.client.store.UserPresence
import net.folivo.trixnity.client.store.eventId
import net.folivo.trixnity.client.user
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.UserId
import net.folivo.trixnity.core.model.events.ClientEvent
import net.folivo.trixnity.core.model.events.m.call.CallEventContent
import net.folivo.trixnity.core.model.events.m.room.MemberEventContent
import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent
import okio.Path.Companion.toPath
import okio.SYSTEM
import okio.buffer
import okio.use
import kotlin.concurrent.Volatile
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.time.TimeSource

private val logger = Logger.withTag("ChatTimelineComponent")

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class RoomComponentImpl(
    componentContext: ComponentContext,
    private val roomId: RoomId,
    private val client: MatrixClient,
    override val contextFactory: ContextFactory, // Injected and exposed
    private val sessionManager: SessionManager,
    override val callModel: CallScreenModel, // 📞 Global Call Model Injected
    private val eventFilter: (TimelineEvent) -> Boolean = DefaultEventFilter
) : ChatTimeline, ComponentContext by componentContext {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main).also {
        lifecycle.doOnDestroy { it.cancel() }
    }

    private val readReceiptSender = ReadReceiptSender(client, scope)
    private val contentSender = RoomContentSender(client, logger)

    private val timelineCoordinator = RoomTimelineCoordinator(
        roomId = roomId,
        client = client,
        scope = scope,
        eventFilter =eventFilter,
        logger = logger
    )

    // ---------------------------------------------------------
    // Outbox
    // ---------------------------------------------------------
    private val outboxFlow: Flow<List<RoomOutboxMessage<*>>> =
        client.room.getOutbox(roomId)
            .map { flows -> flows.mapNotNull { it.firstOrNull() } }
            .distinctUntilChanged()

    private fun isCallEvent(content: Any?): Boolean =
        content is CallEventContent.Invite ||
                content is CallEventContent.Answer ||
                content is CallEventContent.Candidates ||
                content is CallEventContent.Hangup

    // ---------------------------------------------------------
    // UI Items (merged)
    // ---------------------------------------------------------
    override val items: StateFlow<List<UITimelineItem>> =
        combine(
            timelineCoordinator.timelineEvents, // 🔑 already filtered & non-null
            outboxFlow
        ) { serverEvents, outbox ->
            mergeTimelineAndOutbox(serverEvents, outbox)
        }
            .flowOn(Dispatchers.Default)
            .distinctUntilChanged(itemListComparer())
            .onEach { uiList ->
                val last = uiList.lastOrNull { it is MessageItem } as? MessageItem
                last?.let { safeSendReadReceipt(it) }
            }
            .stateIn(scope, SharingStarted.Eagerly, emptyList())


    override val isLoadingBefore: StateFlow<Boolean> =
        timelineCoordinator.isLoadingBefore

    override val isLoadingAfter: StateFlow<Boolean> =
        timelineCoordinator.isLoadingAfter

    override val isInitialLoadComplete: StateFlow<Boolean> =
        timelineCoordinator.isInitialLoadComplete

    // Permissions
    override val microphonePermission: MicrophonePermission = MicrophonePermissionImpl(contextFactory)
    override val cameraPermission: CameraPermission = CameraPermissionImpl(contextFactory)

    // Voice Recorder State
    private val _voiceRecorderState = MutableStateFlow(VoiceRecorderUiState())
    override val voiceRecorderState: StateFlow<VoiceRecorderUiState> = _voiceRecorderState.asStateFlow()
    private var recordingJob: Job? = null

    private val audioRecorder: AudioRecorder = AudioRecorderImpl()
    private var currentRecordingFile: PlatformFile? = null

    // Audio Player State
    private val audioPlayer: AudioPlayer = AudioPlayerImpl()
    private val _currentlyPlayingEventId = MutableStateFlow<String?>(null)
    override val currentlyPlayingEventId: StateFlow<String?> = _currentlyPlayingEventId.asStateFlow()

    private val _currentPlaybackPosition = MutableStateFlow(0L)
    override val currentPlaybackPosition: StateFlow<Long> = _currentPlaybackPosition.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private var playbackJob: Job? = null

    // Video Player State
    private val _playingVideoUrl = MutableStateFlow<String?>(null)
    override val playingVideoUrl: StateFlow<String?> = _playingVideoUrl.asStateFlow()
    private var isVideoLoading = false

    private val userStates = mutableMapOf<UserId, StateFlow<RoomUser?>>()

    @Volatile
    private var lastLoadBeforeMark = TimeSource.Monotonic.markNow()

    init {
        logger.d { "🔥 Init ChatTimelineComponent for ${roomId.full}" }

        scope.launch {
            timelineCoordinator.start()
        }

        // Init Audio Player Listener
        audioPlayer.addListener(object : PlayerListener {
            override fun onProgress(positionMs: Long) {
                // Not used if polling, but if supported by impl
            }

            override fun onCompletion() {
                _currentlyPlayingEventId.value = null
                _isPlaying.value = false
                _currentPlaybackPosition.value = 0L
                playbackJob?.cancel()
            }

            override fun onStateChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                if (isPlaying) {
                    startPlaybackJob()
                } else {
                    playbackJob?.cancel()
                }
            }
        })

        lifecycle.doOnDestroy {
            audioPlayer.release()
            audioRecorder.cancel()
            currentRecordingFile?.delete()

            // Cleanup Call Resources
            callModel.reset()
        }
    }

    // ---------------------------------------------------------
    // Pagination
    // ---------------------------------------------------------
    override suspend fun loadBefore() {
        // Only load if more than 350ms passed
        if (lastLoadBeforeMark.elapsedNow() < 350.milliseconds) return

        lastLoadBeforeMark = TimeSource.Monotonic.markNow()

        timelineCoordinator.loadBefore()
    }

    override suspend fun loadAfter() {
        timelineCoordinator.loadAfter()
    }

    // ---------------------------------------------------------
    // Send Message
    // ---------------------------------------------------------

    override suspend fun sendMessage(text: String) {
        scope.launch {
            // We use the contentSender just for "success" callback if needed, 
            // but Trixnity handles the actual queueing via client.room.sendMessage
            contentSender.sendMessage(roomId, text) {
                loadAfter()
            }
        }
    }

    override suspend fun retryMessage(transactionId: String) {
        scope.launch {
            logger.d { "Retrying message $transactionId" }
            client.room.retrySendMessage(roomId, transactionId)
        }
    }



    // ---------------------------------------------------------
    // Typing Indicators
    // ---------------------------------------------------------
    @OptIn(ExperimentalCoroutinesApi::class)
    override val usersTyping: StateFlow<List<String>> =
        client.room.usersTyping
            .map { typingMap ->
                // Fix: property is 'users' (Set<UserId>)
                typingMap[roomId]?.users.orEmpty()
            }
            .distinctUntilChanged()
            .flatMapLatest { userIds ->
                if (userIds.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    // Filter out self
                    val others = userIds.filter { it != client.userId }
                    if (others.isEmpty()) flowOf(emptyList())
                    else {
                        val nameFlows = others.map { userId ->
                            client.user.getById(roomId, userId)
                                .map { roomUser -> 
                                    // RoomUser has 'name' but not 'displayName'
                                    roomUser?.name ?: userId.full 
                                }
                        }
                        combine(nameFlows) { names -> names.toList() }
                    }
                }
            }
            .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ---------------------------------------------------------
    // User Presence
    // ---------------------------------------------------------
    @OptIn(ExperimentalCoroutinesApi::class)
    override val otherMemberPresence: StateFlow<UserPresence?> =
        client.room.getById(roomId)
            .map { it?.name?.heroes?.firstOrNull() } // Get the Other User ID (DM)
            .distinctUntilChanged()
            .flatMapLatest { userId ->
                if (userId == null) flowOf(null)
                else client.user.getPresence(userId)
            }
            .stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

    // ---------------------------------------------------------
    // Video Message Handling
    // ---------------------------------------------------------
    override suspend fun sendVideoMessage(file: PlatformFile, duration: Long, thumbnail: PlatformFile?) {
        scope.launch {
            contentSender.sendVideo(roomId, file, duration, thumbnail) {
                loadAfter()
            }
        }
    }

    override fun onCallBubbleClicked(
        callId: String,
        isVideo: Boolean,
        isIncoming: Boolean
    ) {
        scope.launch {
            logger.d { "📞 Call bubble clicked: $callId" }

            callModel.resumeIncomingCall(
                callId = callId,
                isVideo = isVideo,
                isIncoming = isIncoming,

                )
        }
    }

    // ---------------------------------------------------------
    // Voice Message Handling
    // ---------------------------------------------------------
    override fun onVoiceMessageEvent(event: VoiceMessageEvent) {
        when (event) {
            VoiceMessageEvent.StartRecording -> {
                scope.launch {
                    if (microphonePermission.hasPermission()) {
                        // Stop playback if recording starts
                        if (_isPlaying.value) {
                            pauseVoiceMessage()
                        }
                        startRecording()
                    } else {
                        val granted = microphonePermission.requestPermission()
                        if (granted) {
                            // Stop playback if recording starts
                            if (_isPlaying.value) {
                                pauseVoiceMessage()
                            }
                            startRecording()
                        } else {
                            // Optionally show rationale or open settings
                            logger.w { "Microphone permission denied" }
                        }
                    }
                }
            }
            VoiceMessageEvent.StopRecording -> {
                stopRecording()
            }
            VoiceMessageEvent.CancelRecording -> {
                cancelRecording()
            }
            VoiceMessageEvent.DismissDialog -> {
                cancelRecording()
            }
            VoiceMessageEvent.SendRecording -> {
                sendRecording()
            }
        }
    }

    // ---------------------------------------------------------
    // Audio Playback
    // ---------------------------------------------------------
    override fun playVoiceMessage(eventId: String, content: RoomMessageEventContent.FileBased.Audio) {
        if (_currentlyPlayingEventId.value == eventId) {
            // Toggle
            if (audioPlayer.isPlaying) {
                pauseVoiceMessage()
            } else {
                audioPlayer.resume()
            }
        } else {
            // New message
            _currentlyPlayingEventId.value = eventId

            scope.launch {
                val url = content.url
                if (url == null) {
                    logger.w { "No URL for audio message" }
                    return@launch
                }

                try {
                    val mediaFlow = client.media.getMedia(url, saveToCache = true).getOrThrow()
                    val mimeType = content.info?.mimeType
                    val extension = MimeTypes.toFileExtension(mimeType)

                    val tempFile = PlatformFile.createTempFile("play_", ".$extension", contextFactory.cacheDir())

                    val sink = okio.FileSystem.SYSTEM.sink(tempFile.path.toPath()).buffer()
                    sink.use { s ->
                        mediaFlow.collect { byteArray ->
                            s.write(byteArray)
                        }
                    }

                    withContext(Dispatchers.Main) {
                        audioPlayer.playFile(tempFile.path)
                        audioPlayer.addListener(object : PlayerListener {
                            override fun onCompletion() {
                                try {
                                    tempFile.delete()
                                    logger.d { "Deleted playback temp file: ${tempFile.path}" }
                                } catch (e: Exception) {
                                    logger.e(e) { "Failed to delete temp playback file" }
                                }
                                audioPlayer.removeListener(this)
                            }

                            override fun onProgress(positionMs: Long) {}
                            override fun onStateChanged(isPlaying: Boolean) {}
                        })
                    }
                } catch (e: Exception) {
                    logger.e(e) { "Failed to download/play audio" }
                }
            }
        }
    }

    override fun pauseVoiceMessage() {
        audioPlayer.pause()
    }

    // ---------------------------------------------------------
    // Video Playback
    // ---------------------------------------------------------
    override fun onPlayVideo(content: RoomMessageEventContent.FileBased.Video) {
        Logger.e("VIDEO MIME TYPE = ${content.info?.mimeType}")
        if (_playingVideoUrl.value != null || isVideoLoading) return
        isVideoLoading = true

        scope.launch {
            try {
                // 1. Get URL (or throw if missing)
                val url = content.url ?: run {
                    logger.w { "No URL for video message" }
                    return@launch
                }

                // 2. Determine file extension from MIME type or fallback
                val mimeType = content.info?.mimeType
                val extension = MimeTypes.toFileExtension(mimeType) // e.g., "mp4"

                // 3. Download to temporary file
                val mediaFlow = client.media.getMedia(url, saveToCache = true).getOrThrow()
                val tempFile = PlatformFile.createTempFile("video_play_", ".$extension", contextFactory.cacheDir())

                val sink = okio.FileSystem.SYSTEM.sink(tempFile.path.toPath()).buffer()
                sink.use { s ->
                    mediaFlow.collect { byteArray ->
                        s.write(byteArray)
                    }
                }

                // 4. Play local file path
                // Important: PlatformFile.path is absolute path on Android/iOS
                // FIX: Append file:// prefix for Android to ensure it's treated as a file URI
                val filePath = tempFile.path
                val playUrl = if (!filePath.startsWith("file://") && !filePath.startsWith("content://")) {
                    "file://$filePath"
                } else {
                    filePath
                }

                _playingVideoUrl.value = playUrl

            } catch (e: Exception) {
                logger.e(e) { "Failed to download/play video" }
            } finally {
                isVideoLoading = false
            }
        }
    }

    override fun onDismissVideoPlayer() {
        _playingVideoUrl.value = null
    }

    private fun startPlaybackJob() {
        playbackJob?.cancel()
        playbackJob = scope.launch {
            while (isActive && audioPlayer.isPlaying) {
                _currentPlaybackPosition.value = audioPlayer.currentPosition
                delay(100)
            }
        }
    }

    private fun startRecording() {
        try {
            if (audioRecorder.isRecording()) return

            val cacheDir = contextFactory.cacheDir()
            currentRecordingFile = PlatformFile.createTempFile("voice_", ".m4a", cacheDir)

            audioRecorder.start(currentRecordingFile!!)

            _voiceRecorderState.value = VoiceRecorderUiState(
                state = VoiceRecorderState.Recording,
                durationSeconds = 0,
                maxDurationSeconds = 60,
                progress = 0f
            )

            recordingJob?.cancel()
            recordingJob = scope.launch {
                val maxSecs = 60
                var currentSecs = 0
                val startTime = TimeSource.Monotonic.markNow()

                while (isActive && currentSecs < maxSecs) {
                    delay(100)

                    val elapsed = startTime.elapsedNow().inWholeMilliseconds
                    val newSecs = (elapsed / 1000).toInt()

                    if (newSecs != currentSecs) {
                        currentSecs = newSecs
                        _voiceRecorderState.update {
                            it.copy(
                                durationSeconds = currentSecs,
                                progress = currentSecs.toFloat() / maxSecs
                            )
                        }
                    }
                }
                if (isActive) {
                    onVoiceMessageEvent(VoiceMessageEvent.StopRecording)
                }
            }
        } catch (e: Exception) {
            logger.e(e) { "Failed to start recording" }
            cancelRecording()
        }
    }

    private fun stopRecording() {
        recordingJob?.cancel()
        try {
            audioRecorder.stop()
        } catch (e: Exception) {
            logger.e(e) { "Failed to stop recording" }
        }
        _voiceRecorderState.update { it.copy(state = VoiceRecorderState.Review) }
    }

    private fun cancelRecording() {
        recordingJob?.cancel()
        try {
            audioRecorder.cancel()
            currentRecordingFile?.delete()
        } catch (e: Exception) {
            logger.e(e) { "Failed to cancel recording" }
        }
        currentRecordingFile = null
        _voiceRecorderState.value = VoiceRecorderUiState(state = VoiceRecorderState.Idle)
    }

    private fun sendRecording() {
        val file = currentRecordingFile
        if (file == null || !file.exists()) {
            cancelRecording()
            return
        }

        val duration = (_voiceRecorderState.value.durationSeconds * 1000).toLong()
        _voiceRecorderState.value = VoiceRecorderUiState(state = VoiceRecorderState.Idle)

        scope.launch {
            contentSender.sendAudio(roomId, file, duration) {
                // file.delete() // Optional cleanup
                currentRecordingFile = null
                loadAfter()
            }
        }
    }


    // ---------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------
    private fun getUserState(id: UserId): StateFlow<RoomUser?> =
        userStates.getOrPut(id) {
            client.user.getById(roomId, id)
                .stateIn(scope, SharingStarted.Lazily, null)
        }

    private fun itemListComparer(): (List<UITimelineItem>, List<UITimelineItem>) -> Boolean =
        { old, new ->
            old.size == new.size &&
                    old.zip(new).all { (a, b) -> a.id == b.id }
        }

    private fun safeSendReadReceipt(item: MessageItem) {
        scope.launch(NonCancellable) {
            try {
                readReceiptSender.markRead(roomId, item.event.eventId)
            } catch (e: Exception) {
                logger.w(e) { "⚠️ markRead failed (ignored)" }
            }
        }
    }

    // -------------------------------------------------------------
    // Convert any TimelineEvent → UI item (call events suppressed)
    // -------------------------------------------------------------
    private fun TimelineEvent.toUiItem(): UITimelineItem {
        val decrypted = content?.getOrNull()

        return when (decrypted) {

            // ---------------------------------------------------------
            // CALL EVENTS ARE IGNORED HERE (handled by call session builder)
            // ---------------------------------------------------------
            is CallEventContent.Invite,
            is CallEventContent.Answer,
            is CallEventContent.Candidates,
            is CallEventContent.Hangup -> {
                // We intentionally don't build a CallItem here.
                EmptyItem(
                    id = eventId.full,
                    timestamp = event.originTimestamp
                )
            }

            // ---------------------------------------------------------
            // NORMAL MESSAGE
            // ---------------------------------------------------------
            is RoomMessageEventContent ->
                MessageItem(
                    event = this,
                    senderId = event.sender,
                    senderState = getUserState(event.sender),
                )

            // ---------------------------------------------------------
            // MEMBERSHIP / STATE EVENT
            // ---------------------------------------------------------
            is MemberEventContent -> {
                val stateEvent = event as? ClientEvent.RoomEvent.StateEvent<*>
                val targetId = stateEvent?.stateKey?.let { UserId(it) } ?: event.sender

                val senderState = getUserState(event.sender)
                val targetState = getUserState(targetId)

                val text = StateEventResolver.resolve(
                    roomId = roomId,
                    timelineEvent = this,
                    currentUserId = client.userId,
                    senderUser = senderState.value,
                    targetUser = targetState.value
                ) ?: "[unknown state event]"

                SystemMessageItem(
                    id = event.id.full,
                    timestamp = event.originTimestamp,
                    text = text
                )
            }

            else -> StateItem(this, getUserState(event.sender))
        }
    }



    // ----------------------------------------------------------------------
// Convert non-call TimelineEvent → UITimelineItem
// (Text messages, media messages, membership changes, system messages)
// ----------------------------------------------------------------------
    private fun TimelineEvent.toUiItemNonCall(): UITimelineItem {
        val decrypted = content?.getOrNull()

        return when (decrypted) {

            // ---------------------------------------------------------
            // NORMAL MESSAGE
            // ---------------------------------------------------------
            is RoomMessageEventContent -> {
                MessageItem(
                    event = this,
                    senderId = event.sender,
                    senderState = getUserState(event.sender)
                )
            }

            // ---------------------------------------------------------
            // MEMBERSHIP / STATE EVENT
            // ---------------------------------------------------------
            is MemberEventContent -> {
                val stateEvent = event as? ClientEvent.RoomEvent.StateEvent<*>
                val targetId = stateEvent?.stateKey?.let { UserId(it) } ?: event.sender

                val senderState = getUserState(event.sender)
                val targetState = getUserState(targetId)

                val text = StateEventResolver.resolve(
                    roomId = roomId,
                    timelineEvent = this,
                    currentUserId = client.userId,
                    senderUser = senderState.value,
                    targetUser = targetState.value
                ) ?: "[unknown state event]"

                SystemMessageItem(
                    id = eventId.full,
                    timestamp = event.originTimestamp,
                    text = text
                )
            }

            // ---------------------------------------------------------
            // FALLBACK
            // ---------------------------------------------------------
            else -> StateItem(
                event = this,
                senderState = getUserState(event.sender)
            )
        }
    }



    // ---------------------------------------------------------
    // Merge timeline + outbox
    // ---------------------------------------------------------
    private fun mergeTimelineAndOutbox(
        serverEvents: List<TimelineEvent>,
        outbox: List<RoomOutboxMessage<*>>
    ): List<UITimelineItem> {

        if (serverEvents.isEmpty() && outbox.isEmpty()) return emptyList()

        val tz = TimeZone.currentSystemDefault()
        val result = ArrayList<UITimelineItem>(serverEvents.size + outbox.size + 10)

        val serverUi = buildUiItemsFromTimeline(serverEvents)
        val hasServerCallBubble =
            serverUi.any { it is CallItem }
        val lastTs = serverEvents.maxOfOrNull { it.event.originTimestamp } ?: 0L

        val outboxUi = outbox
            .filter { msg ->
                val content = msg.content
                if (isCallEvent(content)) return@filter false

                msg.sendError != null ||
                        (msg.eventId == null &&
                                msg.sentAt == null &&
                                msg.createdAt.toEpochMilliseconds() > lastTs)
            }
            .map { msg ->
                val content = msg.content as? RoomMessageEventContent
                OutboxItem(
                    id = msg.transactionId,
                    content = content,
                    text = content?.let { MessageEventFormatter.formatTimeline(it) } ?: "Sending...",
                    timestamp = msg.createdAt.toEpochMilliseconds(),
                    isError = msg.sendError != null,
                    isSent = false
                )
            }

        // 🔹 Step 2: Synthetic call bubble
        val callState = callModel.state.value
        val syntheticCallItem =
            if (!hasServerCallBubble) {
                when (val cs = callState.callState) {

                    is CallState.OutgoingRinging -> {
                        SyntheticCallItem(
                            timestamp = Clock.System.now().toEpochMilliseconds(),
                            type = CallBubbleType.OUTGOING_RINGING,
                            isVideo = callState.isVideoCall
                        )
                    }

                    is CallState.IncomingRinging -> {
                        SyntheticCallItem(
                            timestamp = Clock.System.now().toEpochMilliseconds(),
                            type = CallBubbleType.INCOMING_RINGING,
                            isVideo = callState.isVideoCall
                        )
                    }

                    else -> null
                }
            } else {
                null
            }


        val base = (serverUi + outboxUi).sortedBy { it.timestamp }

        val combined =
            if (syntheticCallItem != null) {
                base + syntheticCallItem
            } else {
                base
            }

        var lastDate: String? = null
        for (item in combined) {
            val date = Instant
                .fromEpochMilliseconds(item.timestamp)
                .toLocalDateTime(tz)
                .date
                .toString()

            if (date != lastDate) {
                result.add(DateItem(item.timestamp))
                lastDate = date
            }
            result.add(item)
        }

        return result
    }


    // Build UI items from server events (messages + call sessions)
    private fun buildUiItemsFromTimeline(
        serverEvents: List<TimelineEvent>
    ): List<UITimelineItem> {
        if (serverEvents.isEmpty()) return emptyList()

        val callEventsById = mutableMapOf<String, MutableList<TimelineEvent>>()
        val nonCallEvents = mutableListOf<TimelineEvent>()

        for (event in serverEvents) {
            when (val content = event.content?.getOrNull()) {
                is CallEventContent.Invite,
                is CallEventContent.Answer,
                is CallEventContent.Candidates,
                is CallEventContent.Hangup -> {
                    val callId = when (content) {
                        is CallEventContent.Invite -> content.callId
                        is CallEventContent.Answer -> content.callId
                        is CallEventContent.Candidates -> content.callId
                        is CallEventContent.Hangup -> content.callId
                        else -> null
                    }
                    if (callId != null) {
                        callEventsById.getOrPut(callId) { mutableListOf() }.add(event)
                    } else {
                        nonCallEvents.add(event)
                    }
                }

                else -> nonCallEvents.add(event)
            }
        }

        // Non-call events → standard mapping
        val nonCallUi = nonCallEvents.map { it.toUiItemNonCall() }

        // Call sessions → 1 bubble per callId
        val callItems = callEventsById.values.mapNotNull { sessionEvents ->
            buildCallItemForSession(sessionEvents)
        }

        return nonCallUi + callItems
    }

    // Build a single CallItem from a group of events sharing the same callId
    private fun buildCallItemForSession(
        events: List<TimelineEvent>
    ): CallItem? {
        if (events.isEmpty()) return null

        val sorted = events.sortedBy { it.event.originTimestamp }

        val inviteEvent = sorted.firstOrNull { it.content?.getOrNull() is CallEventContent.Invite }
            ?: return null

        val inviteContent = inviteEvent.content?.getOrNull() as? CallEventContent.Invite
            ?: return null

        val answerEvent = sorted.firstOrNull { it.content?.getOrNull() is CallEventContent.Answer }
        val hangupEvent = sorted.lastOrNull { it.content?.getOrNull() is CallEventContent.Hangup }
        val hangupContent = hangupEvent?.content?.getOrNull() as? CallEventContent.Hangup

        val myUserId = client.userId
        val isOutgoing = inviteEvent.event.sender == myUserId

        val isVideo = isVideoOffer(inviteContent.offer.sdp)

        // We anchor the bubble at the hangup time if present, otherwise at invite time
        val representativeEvent = hangupEvent ?: inviteEvent
        val timestamp = representativeEvent.event.originTimestamp

        // ❗ Correct usage:
        val id = representativeEvent.eventId.full

        // Duration from answer → hangup (if both exist)
        val durationMs: Long? =
            if (answerEvent != null && hangupEvent != null) {
                (hangupEvent.event.originTimestamp - answerEvent.event.originTimestamp)
                    .takeIf { it > 0 }
            } else null

        val type = resolveCallBubbleType(
            isOutgoing = isOutgoing,
            hasAnswer = answerEvent != null,
            hangupEvent = hangupEvent,
            hangupContent = hangupContent
        )

        val isMissed =
            type == CallBubbleType.INCOMING_MISSED || type == CallBubbleType.OUTGOING_MISSED

        val senderId = inviteEvent.event.sender

        return CallItem(
            id = id,
            timestamp = timestamp,
            senderId = senderId,
            type = type,
            isVideo = isVideo,
            durationMs = durationMs,
            isMissed = isMissed
        )
    }


    private fun resolveCallBubbleType(
        isOutgoing: Boolean,
        hasAnswer: Boolean,
        hangupEvent: TimelineEvent?,
        hangupContent: CallEventContent.Hangup?
    ): CallBubbleType {
        val reason = hangupContent?.reason

        // No answer case
        if (!hasAnswer) {
            if (hangupEvent == null) {
                // Just an invite with no visible hangup yet
                return if (isOutgoing) {
                    CallBubbleType.OUTGOING_RINGING
                } else {
                    CallBubbleType.INCOMING_RINGING
                }
            }

            // There was a hangup without answer → early termination
            return if (isOutgoing) {
                when (reason) {
                    CallEventContent.Hangup.Reason.USER_BUSY ->
                        CallBubbleType.OUTGOING_MISSED

                    CallEventContent.Hangup.Reason.USER_HANGUP ->
                        CallBubbleType.OUTGOING_CANCELLED

                    CallEventContent.Hangup.Reason.ICE_FAILED,
                    CallEventContent.Hangup.Reason.ICE_TIMEOUT,
                    CallEventContent.Hangup.Reason.USER_MEDIA_FAILED ->
                        CallBubbleType.FAILED

                    else -> CallBubbleType.OUTGOING_CANCELLED
                }
            } else {
                when (reason) {
                    CallEventContent.Hangup.Reason.USER_BUSY ->
                        CallBubbleType.INCOMING_MISSED

                    CallEventContent.Hangup.Reason.USER_HANGUP ->
                        // Remote hung up before I answered → missed from my POV
                        CallBubbleType.INCOMING_MISSED

                    CallEventContent.Hangup.Reason.ICE_FAILED,
                    CallEventContent.Hangup.Reason.ICE_TIMEOUT,
                    CallEventContent.Hangup.Reason.USER_MEDIA_FAILED ->
                        CallBubbleType.FAILED

                    else -> CallBubbleType.INCOMING_MISSED
                }
            }
        }

        // Answer exists → it was a "real" call
        return if (isOutgoing) {
            CallBubbleType.OUTGOING_ENDED
        } else {
            CallBubbleType.INCOMING_ENDED
        }
    }

    @Composable
    override fun Render() {}
}
