package com.example.getsafenowclient.room.sharing

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import co.touchlab.kermit.Logger
import com.example.getsafenowclient.camera.VideoRecorderImpl
import com.example.getsafenowclient.permissions.CameraPermission
import com.example.getsafenowclient.permissions.MicrophonePermission
import io.getsafenow.libraries.kmputils.file.safeDelete
import io.getsafenow.libraries.kmputils.platformkmp.ContextFactory
import io.getsafenow.libraries.kmputils.platformkmp.PlatformFile
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

@OptIn(ExperimentalTime::class)
@Composable
fun VideoMessagePresenter(
    contextFactory: ContextFactory,
    cameraPermission: CameraPermission,
    microphonePermission: MicrophonePermission,
    onSendVideo: (PlatformFile, Int, PlatformFile?) -> Unit, // CHANGED: Added thumbnail
    onDismiss: () -> Unit
): Triple<VideoRecorderUiState, (VideoMessageEvent) -> Unit, @Composable () -> Unit> {

    val videoRecorder = remember { VideoRecorderImpl() }
    val scope = rememberCoroutineScope()
    
    var state by remember { mutableStateOf(VideoRecorderState.Idle) }
    var durationSeconds by remember { mutableStateOf(0) }
    val maxDurationSeconds = 60
    var currentFile by remember { mutableStateOf<PlatformFile?>(null) }
    var currentThumbnail by remember { mutableStateOf<PlatformFile?>(null) } // Added state for thumbnail
    var permissionsGranted by remember { mutableStateOf(false) }
    
    // Track if recorder is ready
    var isRecorderReady by remember { mutableStateOf(false) }

    // Release recorder resources when the presenter leaves the composition
    DisposableEffect(Unit) {
        onDispose {
            videoRecorder.release()
        }
    }

    fun createOutputFile(): PlatformFile {
        // Create a unique filename in the cache directory
        val fileName = "video_msg_${Clock.System.now().toEpochMilliseconds()}.mp4"
        return PlatformFile(contextFactory.cacheDir(), fileName)
    }

    // Request permissions on start
    LaunchedEffect(Unit) {
        // Check Camera
        if (!cameraPermission.hasPermission()) {
            val granted = cameraPermission.requestPermission()
            if (!granted) {
                onDismiss()
                return@LaunchedEffect
            }
        }
        // Check Microphone
        if (!microphonePermission.hasPermission()) {
            val granted = microphonePermission.requestPermission()
            if (!granted) {
                onDismiss()
                return@LaunchedEffect
            }
        }
        // All granted
        permissionsGranted = true
    }
    
    // Poll for recorder readiness once permissions are granted
    LaunchedEffect(permissionsGranted) {
        if (permissionsGranted) {
            while (!isRecorderReady) {
                if (videoRecorder.isReady()) {
                    isRecorderReady = true
                } else {
                    delay(100)
                }
            }
        }
    }

    // Auto-start recording when permissions are granted AND recorder is ready
    // Replace the auto-start LaunchedEffect in VideoMessagePresenter.kt with this:

// Auto-start recording when permissions are granted AND recorder is ready
    LaunchedEffect(permissionsGranted, isRecorderReady) {
        if (permissionsGranted && isRecorderReady && state == VideoRecorderState.Idle) {
            // CRITICAL: Wait for preview to fully render and session to stabilize
            Logger.i("Waiting for preview to stabilize before recording...")
            delay(300) // Give preview 1 second to fully render

            if (!videoRecorder.isReady()) {
                Logger.e("Recorder not ready after delay")
                onDismiss()
                return@LaunchedEffect
            }

            Logger.i("Starting auto-record")
            val file = createOutputFile()
            currentFile = file
            currentThumbnail = null
            videoRecorder.start(file) { thumb, error ->
                if (error != null) {
                    Logger.e("Recording error: $error")
                    scope.launch { state = VideoRecorderState.Idle }
                } else {
                    scope.launch {
                        currentThumbnail = thumb
                        state = VideoRecorderState.Review
                    }
                }
            }
            state = VideoRecorderState.Recording
        }
    }

    // Timer Logic
    LaunchedEffect(state) {
        if (state == VideoRecorderState.Recording) {
            val startTime = TimeSource.Monotonic.markNow()
            while (state == VideoRecorderState.Recording) {
                val elapsed = startTime.elapsedNow()
                durationSeconds = elapsed.inWholeSeconds.toInt()
                
                if (durationSeconds >= maxDurationSeconds) {
                    // Auto-stop
                    videoRecorder.stop()
                    state = VideoRecorderState.Loading
                }
                
                delay(100) // Update frequently for smoother progress
            }
        } else if (state == VideoRecorderState.Idle) {
            durationSeconds = 0
        }
    }

    val onEvent: (VideoMessageEvent) -> Unit = { event ->
        when (event) {
            VideoMessageEvent.StartRecording -> {
                if (state == VideoRecorderState.Idle && permissionsGranted && isRecorderReady) {
                    val file = createOutputFile()
                    currentFile = file
                    currentThumbnail = null
                    videoRecorder.start(file) { thumb, error ->
                        if (error != null) {
                            Logger.e("Recording error: $error")
                            scope.launch { state = VideoRecorderState.Idle }
                        } else {
                             scope.launch {
                                 currentThumbnail = thumb
                                 state = VideoRecorderState.Review
                             }
                        }
                    }
                    state = VideoRecorderState.Recording
                }
            }
            VideoMessageEvent.StopRecording -> {
                if (state == VideoRecorderState.Recording) {
                    videoRecorder.stop()
                    state = VideoRecorderState.Loading
                }
            }
            VideoMessageEvent.CancelRecording -> {
                videoRecorder.cancel()
                // If we are cancelling, we delete the file and return to Idle
                currentFile?.safeDelete()
                currentFile = null
                currentThumbnail?.safeDelete()
                currentThumbnail = null
                state = VideoRecorderState.Idle
                onDismiss()
            }
            VideoMessageEvent.SendRecording -> {
                if (state == VideoRecorderState.Review) {
                    currentFile?.let { file ->
                        onSendVideo(file, durationSeconds, currentThumbnail)
                    }
                }
            }
            VideoMessageEvent.SwitchCamera -> {
                videoRecorder.switchCamera()
            }
            VideoMessageEvent.DismissDialog -> {
                videoRecorder.cancel()
                currentFile?.safeDelete()
                currentThumbnail?.safeDelete()
                onDismiss()
            }
        }
    }

    val uiState = VideoRecorderUiState(
        state = state,
        durationSeconds = durationSeconds,
        maxDurationSeconds = maxDurationSeconds,
        progress = if (maxDurationSeconds > 0) durationSeconds.toFloat() / maxDurationSeconds.toFloat() else 0f
    )

    // Only show camera preview if permissions are granted
    val cameraPreview: @Composable () -> Unit = {
        // Always render the camera preview composable as long as we have permissions,
        // even if not recording, so the view system initializes
        if (permissionsGranted && state != VideoRecorderState.Review) {
            videoRecorder.CameraPreview(Modifier.fillMaxSize())
        }
    }

    return Triple(uiState, onEvent, cameraPreview)
}
