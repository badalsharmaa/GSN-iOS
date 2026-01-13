package com.example.getsafenowclient.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import co.touchlab.kermit.Logger
import io.getsafenow.libraries.kmputils.platformkmp.PlatformFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual class VideoRecorderImpl : VideoRecorder {
    private var cameraProvider: ProcessCameraProvider? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var activeRecording: Recording? = null
    private var previewView: PreviewView? = null
    private var lifecycleOwner: LifecycleOwner? = null
    
    private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
    private var _isRecording by mutableStateOf(false)
    private var _isReady by mutableStateOf(false)

    actual override fun start(outputFile: PlatformFile, onRecordingFinished: (thumbnail: PlatformFile?, error: Throwable?) -> Unit) {
        val videoCapture = this.videoCapture
        if (videoCapture == null) {
            // Not ready, but let's not crash or call back error immediately if we can retry or wait.
            // Actually, if we aren't ready, we can't start.
            Logger.e("VideoRecorderImpl: start() called but videoCapture is null. Aborting.")
            onRecordingFinished(null, IllegalStateException("Camera not initialized"))
            return
        }
        val view = previewView
        if (view == null) {
            Logger.e("VideoRecorderImpl: start() called but previewView is null. Aborting.")
            onRecordingFinished(null, IllegalStateException("Preview view not available"))
            return
        }
        
        if (activeRecording != null) {
            activeRecording?.stop()
            activeRecording = null
        }

        val file = File(outputFile.path)
        file.parentFile?.mkdirs()

        val outputOptions = FileOutputOptions.Builder(file).build()

        try {
            val hasAudioPermission = ContextCompat.checkSelfPermission(
                view.context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

            val pendingRecording = videoCapture.output
                .prepareRecording(view.context, outputOptions)

            if (hasAudioPermission) {
                pendingRecording.withAudioEnabled()
            }

            activeRecording = pendingRecording.start(ContextCompat.getMainExecutor(view.context)) { event ->
                when (event) {
                    is VideoRecordEvent.Start -> {
                        _isRecording = true
                    }
                    is VideoRecordEvent.Finalize -> {
                        _isRecording = false
                        if (event.hasError()) {
                            Logger.e("Video recording error code: ${event.error}")
                            onRecordingFinished(null, event.cause)
                        } else {
                            // Generate thumbnail asynchronously
                            generateThumbnail(file) { thumbFile, err ->
                                onRecordingFinished(thumbFile, err)
                            }
                        }
                    }
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            onRecordingFinished(null, e)
        } catch (e: Exception) {
            e.printStackTrace()
            onRecordingFinished(null, e)
        }
    }

    private fun generateThumbnail(videoFile: File, callback: (PlatformFile?, Throwable?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(videoFile.absolutePath)
                // Extract frame at 0 seconds
                val bitmap = retriever.getFrameAtTime(0) ?: throw Exception("Failed to retrieve frame")
                retriever.release()
                
                // Save thumbnail to file
                val thumbFile = File(videoFile.parentFile, "thumb_${videoFile.nameWithoutExtension}.jpg")
                val out = FileOutputStream(thumbFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
                out.flush()
                out.close()
                
                callback(PlatformFile(thumbFile.absolutePath), null)
            } catch (e: Exception) {
                e.printStackTrace()
                // Return success for video even if thumbnail fails, but pass null thumb
                callback(null, null) 
            }
        }
    }

    actual override fun stop() {
        activeRecording?.stop()
        activeRecording = null
    }

    actual override fun cancel() {
        stop()
    }

    actual override fun switchCamera() {
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
            CameraSelector.DEFAULT_BACK_CAMERA
        } else {
            CameraSelector.DEFAULT_FRONT_CAMERA
        }
        bindCameraUseCases()
    }

    actual override fun isRecording(): Boolean = _isRecording
    
    actual override fun isReady(): Boolean = _isReady

    actual override fun release() {
        stop()
        cameraProvider?.unbindAll()
        cameraProvider = null
        _isReady = false
    }

    @Composable
    actual override fun CameraPreview(modifier: Modifier) {
        val context = LocalContext.current
        val lifecycle = LocalLifecycleOwner.current
        
        this.lifecycleOwner = lifecycle

        AndroidView(
            modifier = modifier,
            factory = { ctx ->
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    previewView = this
                }
            },
            update = {
                 previewView = it
            }
        )

        LaunchedEffect(cameraSelector) {
            if (cameraProvider == null) {
                cameraProvider = getCameraProvider(context)
            }
            bindCameraUseCases()
        }
    }
    
    private fun bindCameraUseCases() {
        val provider = cameraProvider ?: return
        val owner = lifecycleOwner ?: return
        val view = previewView ?: return

        provider.unbindAll()

        val preview = Preview.Builder()
            .build()
            .also { it.surfaceProvider = view.surfaceProvider }

        // CHANGED: Use Quality.SD (480p) or HD (720p) instead of HIGHEST.
        // SD (480p) is generally sufficient for chat bubbles and quick sharing.
        // Fallback strategy: Try SD -> HD -> Lowest available.
/*        val qualitySelector = QualitySelector.fromOrderedList(
            listOf(Quality.SD, Quality.HD, Quality.LOWEST)
        )*/

        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.SD))
            .build()
        
        videoCapture = VideoCapture.withOutput(recorder)

        try {
            provider.bindToLifecycle(
                owner,
                cameraSelector,
                preview,
                videoCapture
            )
            _isReady = true
        } catch (e: Exception) {
            e.printStackTrace()
            _isReady = false
        }
    }

    private suspend fun getCameraProvider(context: Context): ProcessCameraProvider =
        suspendCoroutine { continuation ->
            val future = ProcessCameraProvider.getInstance(context)
            future.addListener({
                continuation.resume(future.get())
            }, ContextCompat.getMainExecutor(context))
        }
}
