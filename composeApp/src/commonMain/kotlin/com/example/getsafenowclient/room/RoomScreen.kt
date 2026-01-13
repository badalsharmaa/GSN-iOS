package com.example.getsafenowclient.room

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.getsafenowclient.camera.VideoPlayerImpl
import com.example.getsafenowclient.component.GsnLoader
import com.example.getsafenowclient.component.MessageInput
import com.example.getsafenowclient.component.RoomHeader
import com.example.getsafenowclient.component.chat.VideoRecordingDialog
import com.example.getsafenowclient.component.chat.VoiceRecordingDialog
import com.example.getsafenowclient.room.sharing.VideoMessageEvent
import com.example.getsafenowclient.room.sharing.VideoMessagePresenter
import com.example.getsafenowclient.room.sharing.VoiceMessageEvent
import com.example.getsafenowclient.room.sharing.VoiceRecorderState
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Regular
import compose.icons.fontawesomeicons.regular.TimesCircle
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.client.room
import net.folivo.trixnity.client.user
import net.folivo.trixnity.core.model.RoomId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomScreen(
    modifier: Modifier = Modifier,
    component: ChatTimeline,
    client: MatrixClient,
    roomId: RoomId,
    onBack: (() -> Unit)?,
    // We ignore these parameters now as we handle them internally with the component
    onStartVoiceCall: () -> Unit = {},
    onStartVideoCall: () -> Unit = {}
) {
    val items by component.items.collectAsState()
    val isLoadingBefore by component.isLoadingBefore.collectAsState()
    val isInitialLoadComplete by component.isInitialLoadComplete.collectAsState()
    val voiceRecorderState by component.voiceRecorderState.collectAsState()
    val playingVideoUrl by component.playingVideoUrl.collectAsState()
    
    // Distance from bottom in pixels (core fix)
    var distanceFromBottomPx by remember { mutableStateOf(0) }

    // Whether user is close enough to bottom to auto-scroll
    val isNearBottom by remember {
        derivedStateOf { distanceFromBottomPx < 48 } // ~1 message height
    }

    var pendingPrepend by remember { mutableStateOf(false) }
    var anchorItemIndex by remember { mutableStateOf(0) }
    var anchorItemOffset by remember { mutableStateOf(0) }


    val roomState by client.room.getById(roomId).collectAsState(null)

    var messageText by remember { mutableStateOf("") }
    var showVideoRecorder by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Track if we've already done the initial scroll
    var hasPerformedInitialScroll by remember { mutableStateOf(false) }

    // Whether user is currently near the bottom
    var shouldStickToBottom by remember { mutableStateOf(true) }

    // -------------------------------------------------------
    // Room Title logic
    // -------------------------------------------------------
    val heroId = roomState?.name?.heroes?.firstOrNull()
    val roomName =
        roomState?.name?.explicitName
            ?: heroId?.let { uid ->
                client.user.getById(roomId, uid).collectAsState(null).value?.name ?: uid.localpart
            }
            ?: "Unknown Room"

    // -------------------------------------------------------
    // Inline top loader visibility
    // -------------------------------------------------------
    val showTopLoader = isLoadingBefore

    // -------------------------------------------------------
    // Track if user is near the bottom of the list
    // -------------------------------------------------------
    LaunchedEffect(listState) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            val total = layoutInfo.totalItemsCount
            lastVisible to total
        }.collect { (lastVisible, total) ->
            shouldStickToBottom = if (total == 0) {
                true
            } else {
                // "Near bottom" = last visible item is within last 2 items
                lastVisible >= total - 2
            }
        }
    }

    // -------------------------------------------------------
    // Initial scroll-to-bottom + keep-at-bottom-on-new-messages
    // -------------------------------------------------------
    LaunchedEffect(isInitialLoadComplete, items.size) {
        if (!isInitialLoadComplete || items.isEmpty()) return@LaunchedEffect

        val lastIndex = items.lastIndex

        if (!hasPerformedInitialScroll) {
            listState.scrollToItem(lastIndex)
            hasPerformedInitialScroll = true
        } else if (shouldStickToBottom) {
            listState.animateScrollToItem(lastIndex)
        }
    }

    LaunchedEffect(items) {
        val layout = listState.layoutInfo
        val totalItems = layout.totalItemsCount
        if (totalItems == 0) return@LaunchedEffect

        if (pendingPrepend) {
            // Restore scroll relative to previously visible item (pagination case)
            listState.scrollToItem(
                index = anchorItemIndex,
                scrollOffset = anchorItemOffset
            )
            pendingPrepend = false
        } else if (!isNearBottom) {
            // Normal structural changes (call bubbles etc.)
            listState.scrollToItem(
                index = totalItems - 1,
                scrollOffset = -distanceFromBottomPx
            )
        }
    }

    // -------------------------------------------------------
    // Pagination (older messages)
    // -------------------------------------------------------
    LaunchedEffect(listState) {
        snapshotFlow {
            Triple(
                listState.firstVisibleItemIndex,
                listState.firstVisibleItemScrollOffset,
                isLoadingBefore
            )
        }
            .distinctUntilChanged()
            .collect { (firstIndex, scrollOffset, loadingBefore) ->

                // 1️⃣ Only trigger when user is REALLY at the top
                val isAtTop = firstIndex == 0 && scrollOffset == 0

                if (isAtTop && !loadingBefore && !pendingPrepend) {

                    // 2️⃣ Capture anchor BEFORE loading more
                    val firstVisible = listState.layoutInfo.visibleItemsInfo.firstOrNull()
                    if (firstVisible != null) {
                        anchorItemIndex = firstVisible.index
                        anchorItemOffset = firstVisible.offset
                        pendingPrepend = true
                    }

                    // 3️⃣ Load older messages
                    component.loadBefore()
                }
            }
    }


    // -------------------------------------------------------
    // UI
    // -------------------------------------------------------
    Scaffold(
        modifier = modifier.fillMaxSize().imePadding(),
        topBar = {
            RoomHeader(
                client = client,
                roomId = roomId.full,
                roomName = roomName,
                roomAvatarUrl = roomState?.avatarUrl,
                roomStatus = "Active now",
                onBackClick = { onBack?.invoke() },
                onStarClick = {}
            )
        },
        bottomBar = {
            MessageInput(
                value = messageText,
                onValueChange = { messageText = it },
                onSendClick = {
                    if (messageText.isBlank()) return@MessageInput
                    val toSend = messageText
                    messageText = ""
                    scope.launch {
                        component.sendMessage(toSend)
                    }
                },
                onStartRecording = {
                    component.onVoiceMessageEvent(VoiceMessageEvent.StartRecording)
                },
                onStartVideoRecording = {
                    showVideoRecorder = true
                },
                onSendImage = { /* TODO: Implement Image Picker */ },
                onSendFile = { /* TODO: Implement File Picker */ },
                // 📞 WIRED UP CALL ACTIONS WITH PERMISSIONS
                onStartVoiceCall = {
                    scope.launch {
                        if (component.microphonePermission.requestPermission()) {
                            component.callModel.startOutgoingCall(roomId, isVideo = false, opponentId = heroId?.full ?: "")
                        }
                    }
                },
                onStartVideoCall = {
                    scope.launch {
                        if (component.cameraPermission.requestPermission() &&
                            component.microphonePermission.requestPermission()
                        ) {
                            component.callModel.startOutgoingCall(roomId, isVideo = true, opponentId = heroId?.full ?: "")
                        }
                    }
                },
                onAddPeople = { /* TODO: Implement Add Member logic */ }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item(key = "top_loader") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (showTopLoader) {
                            GsnLoader(size = 28)
                        }
                    }
                }

                itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
                    val prev = items.getOrNull(index - 1)
                    val next = items.getOrNull(index + 1)

                    val isFirstInBlock = prev?.let {
                        it !is MessageItem || it.senderId != (item as? MessageItem)?.senderId
                    } ?: true

                    val isLastInBlock = next?.let {
                        it !is MessageItem || it.senderId != (item as? MessageItem)?.senderId
                    } ?: true

                    item.render(
                        modifier = Modifier,
                        client = client,
                        component = component,
                        isFirstInBlock = isFirstInBlock,
                        isLastInBlock = isLastInBlock
                    )
                }
            }
        }
    }

    // -------------------------------------------------------
    // Voice Recording Dialog
    // -------------------------------------------------------
    if (voiceRecorderState.state != VoiceRecorderState.Idle) {
        VoiceRecordingDialog(
            uiState = voiceRecorderState,
            onStop = { component.onVoiceMessageEvent(VoiceMessageEvent.StopRecording) },
            onCancel = { component.onVoiceMessageEvent(VoiceMessageEvent.CancelRecording) },
            onSend = { component.onVoiceMessageEvent(VoiceMessageEvent.SendRecording) },
            onDismiss = { component.onVoiceMessageEvent(VoiceMessageEvent.DismissDialog) }
        )
    }

    // -------------------------------------------------------
    // Video Recorder
    // -------------------------------------------------------
    if (showVideoRecorder) {
        val (videoState, onVideoEvent, cameraPreview) = VideoMessagePresenter(
            contextFactory = component.contextFactory,
            cameraPermission = component.cameraPermission,
            microphonePermission = component.microphonePermission,
            onSendVideo = { file, duration, thumbnail ->
                scope.launch {
                    component.sendVideoMessage(file, duration.toLong() * 1000, thumbnail)
                }
                showVideoRecorder = false
            },
            onDismiss = { showVideoRecorder = false }
        )

        VideoRecordingDialog(
            uiState = videoState,
            cameraPreview = cameraPreview,
            onStop = { onVideoEvent(VideoMessageEvent.StopRecording) },
            onCancel = { onVideoEvent(VideoMessageEvent.CancelRecording) },
            onSend = { onVideoEvent(VideoMessageEvent.SendRecording) },
            onDismiss = { onVideoEvent(VideoMessageEvent.DismissDialog) }
        )
    }

    // -------------------------------------------------------
    // Full Screen Video Player
    // -------------------------------------------------------
    playingVideoUrl?.let { url ->
        Dialog(
            onDismissRequest = { component.onDismissVideoPlayer() },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                val videoPlayer = remember(url) { VideoPlayerImpl() }

                DisposableEffect(url) {
                    if (url.startsWith("file://")) {
                        val path = url.removePrefix("file://")
                        videoPlayer.playFile(path)
                    } else {
                        videoPlayer.play(url)
                    }
                    onDispose {
                        videoPlayer.release()
                    }
                }

                videoPlayer.VideoView(Modifier.fillMaxSize())

                IconButton(
                    onClick = { component.onDismissVideoPlayer() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = FontAwesomeIcons.Regular.TimesCircle,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}
