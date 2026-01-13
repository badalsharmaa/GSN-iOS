package com.example.getsafenowclient.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import co.touchlab.kermit.Logger
import io.getsafenow.libraries.kmputils.platformkmp.PlatformFile
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import kotlinx.coroutines.launch
import platform.AVFoundation.*
import platform.CoreGraphics.CGRectZero
import platform.CoreMedia.CMTimeMake
import platform.Foundation.*
import platform.QuartzCore.CATransaction
import platform.QuartzCore.kCATransactionDisableActions
import platform.UIKit.*
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@OptIn(ExperimentalForeignApi::class)
actual class VideoRecorderImpl : VideoRecorder {
    private val session = AVCaptureSession()
    private val movieOutput = AVCaptureMovieFileOutput()
    private var videoInput: AVCaptureDeviceInput? = null
    private var audioInput: AVCaptureDeviceInput? = null

    private var recordingDelegate: RecordingDelegate? = null

    private var _isRecording by mutableStateOf(false)
    private var _isReady by mutableStateOf(false)
    private var cameraPosition = AVCaptureDevicePositionFront

    // Track if session is configured to prevent double configuration
    private var isConfigured = false

    init {
        Logger.i("VideoRecorderImpl: Initializing iOS video recorder")
        configureSession()
    }

    private fun configureSession() {
        if (isConfigured) {
            Logger.w("Session already configured, skipping")
            return
        }

        Logger.i("Configuring AVCaptureSession...")
        session.beginConfiguration()

        // Use preset that works on iPad iOS 15.8+ (Prioritizing compatibility over highest quality)
        val presetToUse = when {
            session.canSetSessionPreset(AVCaptureSessionPreset640x480) -> {
                Logger.i("Using 640x480 preset (most compatible)")
                AVCaptureSessionPreset640x480
            }
            session.canSetSessionPreset(AVCaptureSessionPresetMedium) -> {
                Logger.i("Using Medium preset")
                AVCaptureSessionPresetMedium
            }
            else -> {
                Logger.e("No compatible preset found, falling back to Low")
                AVCaptureSessionPresetLow
            }
        }
        session.sessionPreset = presetToUse

        // 1. Setup Camera Input
        val cameraSuccess = setupCameraInput(cameraPosition)
        if (!cameraSuccess) {
            Logger.e("Failed to setup camera input")
            session.commitConfiguration()
            return
        }

        // 2. Setup Audio Input (critical - must be added BEFORE output)
        val audioSuccess = setupAudioInput()
        if (!audioSuccess) {
            Logger.w("Failed to setup audio input, continuing without audio")
        }

        // 3. Setup Movie Output
        if (session.canAddOutput(movieOutput)) {
            session.addOutput(movieOutput)

            val connection = movieOutput.connectionWithMediaType(AVMediaTypeVideo)
            if (connection != null) {
                // FIX: Use AVCaptureVideoStabilizationModeStandard for compatibility
                if (connection.isVideoStabilizationSupported()) {
                    connection.preferredVideoStabilizationMode = AVCaptureVideoStabilizationModeStandard
                    Logger.i("Video stabilization set to Standard for compatibility")
                }
            }

            // Set max duration to prevent issues
            movieOutput.maxRecordedDuration = CMTimeMake(120, 1) // 120 seconds max
            movieOutput.minFreeDiskSpaceLimit = 1024 * 1024 * 50 // 50MB min space

            Logger.i("Movie output added successfully")
        } else {
            Logger.e("Cannot add movie output to session")
            session.commitConfiguration()
            return
        }

        session.commitConfiguration()
        isConfigured = true

        // FIX: Start session immediately after configuration for stable preview
        session.startRunning()
        _isReady = true
        Logger.i("Session configured and started running")
    }

    private fun setupCameraInput(position: AVCaptureDevicePosition): Boolean {
        // Remove existing video input
        videoInput?.let {
            if (session.inputs.contains(it)) {
                session.removeInput(it)
            }
        }
        videoInput = null

        // FIX: Use a filtered set of device types for broader compatibility
        val deviceTypes = when (position) {
            AVCaptureDevicePositionFront -> listOf(
                AVCaptureDeviceTypeBuiltInWideAngleCamera,
                AVCaptureDeviceTypeBuiltInTrueDepthCamera
            )
            else -> listOf(
                AVCaptureDeviceTypeBuiltInWideAngleCamera,
                AVCaptureDeviceTypeBuiltInTelephotoCamera,
                AVCaptureDeviceTypeBuiltInDualCamera
            )
        }

        val discoverySession = AVCaptureDeviceDiscoverySession.discoverySessionWithDeviceTypes(
            deviceTypes = deviceTypes,
            mediaType = AVMediaTypeVideo,
            position = position
        )

        val device = discoverySession?.devices?.firstOrNull() as? AVCaptureDevice

        if (device == null) {
            Logger.e("No camera device found for position: $position")
            return false
        }

        Logger.i("Found camera device: ${device.localizedName}")

        // FIX: Remove invalid activeVideoMin/MaxFrameDuration settings

        val input = AVCaptureDeviceInput.deviceInputWithDevice(device, null)
        if (input == null) {
            Logger.e("Failed to create device input")
            return false
        }

        if (session.canAddInput(input)) {
            session.addInput(input)
            videoInput = input
            Logger.i("Video input added successfully")
            return true
        } else {
            Logger.e("Cannot add video input to session")
            return false
        }
    }

    private fun setupAudioInput(): Boolean {
        // Remove existing audio input
        audioInput?.let {
            if (session.inputs.contains(it)) {
                session.removeInput(it)
            }
        }
        audioInput = null

        val audioDevice = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeAudio)
        if (audioDevice == null) {
            Logger.w("No audio device found")
            return false
        }

        val input = AVCaptureDeviceInput.deviceInputWithDevice(audioDevice, null)
        if (input == null) {
            Logger.w("Failed to create audio input")
            return false
        }

        if (session.canAddInput(input)) {
            session.addInput(input)
            audioInput = input
            Logger.i("Audio input added successfully")
            return true
        } else {
            Logger.w("Cannot add audio input")
            return false
        }
    }

    actual override fun start(outputFile: PlatformFile, onRecordingFinished: (thumbnail: PlatformFile?, error: Throwable?) -> Unit) {
        Logger.i("VideoRecorder.start() called")

        // Check if already recording
        if (movieOutput.isRecording()) {
            Logger.w("Already recording, stopping previous recording first")
            movieOutput.stopRecording()
            dispatch_async(dispatch_get_main_queue()) {
                kotlinx.coroutines.GlobalScope.launch {
                    kotlinx.coroutines.delay(500)
                    start(outputFile, onRecordingFinished)
                }
            }
            return
        }

        // Verify session is running (should be running from init)
        if (!session.running) {
            Logger.w("Session is not running, attempting to start now...")
            session.startRunning()
            dispatch_async(dispatch_get_main_queue()) {
                kotlinx.coroutines.GlobalScope.launch {
                    kotlinx.coroutines.delay(500)
                    start(outputFile, onRecordingFinished)
                }
            }
            return
        }

        // ... (File management logic remains the same) ...
        val url = NSURL.fileURLWithPath(outputFile.path)
        val fileManager = NSFileManager.defaultManager

        val dirUrl = url.URLByDeletingLastPathComponent
        if (dirUrl != null && !fileManager.fileExistsAtPath(dirUrl.path!!)) {
            try {
                fileManager.createDirectoryAtPath(dirUrl.path!!, true, null, null)
                Logger.i("Created directory: ${dirUrl.path}")
            } catch (e: Exception) {
                Logger.e("Failed to create directory: ${e.message}")
                onRecordingFinished(null, e)
                return
            }
        }

        if (fileManager.fileExistsAtPath(outputFile.path)) {
            try {
                fileManager.removeItemAtPath(outputFile.path, null)
                Logger.i("Deleted existing file")
            } catch (e: Exception) {
                Logger.w("Could not delete existing file: ${e.message}")
            }
        }
        // ... (End of File management logic) ...

        // CRITICAL: Verify output connections before recording
        val videoConnection = movieOutput.connectionWithMediaType(AVMediaTypeVideo)
        if (videoConnection == null) {
            Logger.e("No video connection available!")
            onRecordingFinished(null, Exception("No video connection"))
            return
        }

        // Configure video connection
        if (videoConnection.isVideoOrientationSupported()) {
            videoConnection.videoOrientation = AVCaptureVideoOrientationPortrait
        }

        if (videoConnection.isVideoMirroringSupported()) {
            videoConnection.videoMirrored = (cameraPosition == AVCaptureDevicePositionFront)
        }

        Logger.i("Video connection configured, starting recording to: ${outputFile.path}")

        // Create and retain delegate
        val delegate = RecordingDelegate { success, savedUrl, error ->
            dispatch_async(dispatch_get_main_queue()) {
                _isRecording = false
                Logger.i("Recording finished callback - success: $success")

                if (success && savedUrl != null) {
                    val thumbPath = generateThumbnail(savedUrl)
                    val thumbFile = thumbPath?.let { PlatformFile(PlatformFile(NSTemporaryDirectory()), it) }
                    onRecordingFinished(thumbFile, null)
                } else {
                    val desc = error?.localizedDescription ?: "Unknown recording error"
                    Logger.e("Recording failed: $desc")
                    onRecordingFinished(null, Exception(desc))
                }
                recordingDelegate = null
            }
        }

        recordingDelegate = delegate

        // Start recording
        try {
            movieOutput.startRecordingToOutputFileURL(url, delegate)
            _isRecording = true
            Logger.i("startRecordingToOutputFileURL called successfully")
        } catch (e: Exception) {
            Logger.e("Exception calling startRecordingToOutputFileURL: ${e.message}")
            _isRecording = false
            recordingDelegate = null
            onRecordingFinished(null, e)
        }
    }

    private fun generateThumbnail(videoUrl: NSURL): String? {
        // ... (Thumbnail generation logic remains the same) ...
        return try {
            val asset = AVAsset.assetWithURL(videoUrl)
            val generator = AVAssetImageGenerator.assetImageGeneratorWithAsset(asset)
            generator.appliesPreferredTrackTransform = true
            generator.requestedTimeToleranceBefore = platform.CoreMedia.kCMTimeZero.readValue()
            generator.requestedTimeToleranceAfter = platform.CoreMedia.kCMTimeZero.readValue()

            val cgImage = generator.copyCGImageAtTime(CMTimeMake(0, 1), null, null)
            if (cgImage == null) {
                Logger.w("Failed to generate thumbnail image")
                return null
            }

            val uiImage = UIImage.imageWithCGImage(cgImage)
            val data = UIImageJPEGRepresentation(uiImage, 0.8)

            if (data == null) {
                Logger.w("Failed to convert image to JPEG")
                return null
            }

            val filename = "thumb_${videoUrl.lastPathComponent}.jpg"
            val path = NSTemporaryDirectory() + filename

            if (data.writeToFile(path, true)) {
                Logger.i("Thumbnail saved: $filename")
                return filename
            } else {
                Logger.w("Failed to write thumbnail file")
                return null
            }
        } catch (e: Exception) {
            Logger.e("Error generating thumbnail: ${e.message}")
            null
        }
    }

    actual override fun stop() {
        // ... (stop() remains the same) ...
        Logger.i("VideoRecorder.stop() called")
        if (movieOutput.isRecording()) {
            movieOutput.stopRecording()
            Logger.i("Recording stopped")
        } else {
            Logger.w("stop() called but not recording")
        }
    }

    actual override fun cancel() {
        // ... (cancel() remains the same) ...
        Logger.i("VideoRecorder.cancel() called")
        stop()
    }

    actual override fun switchCamera() {
        // ... (switchCamera() remains the same) ...
        Logger.i("Switching camera...")
        session.beginConfiguration()

        cameraPosition = if (cameraPosition == AVCaptureDevicePositionFront) {
            AVCaptureDevicePositionBack
        } else {
            AVCaptureDevicePositionFront
        }

        setupCameraInput(cameraPosition)
        session.commitConfiguration()

        Logger.i("Camera switched to: ${if (cameraPosition == AVCaptureDevicePositionFront) "front" else "back"}")
    }

    actual override fun isRecording(): Boolean = _isRecording

    actual override fun isReady(): Boolean = _isReady

    actual override fun release() {
        // ... (release() remains the same) ...
        Logger.i("Releasing video recorder resources...")
        stop()
        recordingDelegate = null

        session.stopRunning()

        // Remove all inputs
        session.inputs.toList().forEach { input ->
            session.removeInput(input as AVCaptureInput)
        }

        // Remove all outputs
        session.outputs.toList().forEach { output ->
            session.removeOutput(output as AVCaptureOutput)
        }

        videoInput = null
        audioInput = null
        _isReady = false
        isConfigured = false

        Logger.i("Video recorder released")
    }

    @Composable
    actual override fun CameraPreview(modifier: Modifier) {
        val previewLayer = remember {
            AVCaptureVideoPreviewLayer(session = session).apply {
                videoGravity = AVLayerVideoGravityResizeAspectFill
            }
        }

        DisposableEffect(previewLayer) {
            Logger.i("CameraPreview composed: Preview layer created")

            // Ensure session is running (already started in init, this is a guard)
            if (!session.running) {
                session.startRunning()
                _isReady = true
                Logger.w("Session started late from CameraPreview DisposableEffect")
            }

            onDispose {
                Logger.i("CameraPreview disposed")
                previewLayer.removeFromSuperlayer()
            }
        }

        UIKitView(
            modifier = modifier,
            factory = {
                // *** CRITICAL FIX: Use the custom UIView subclass from the old working code ***
                val container = object : UIView(frame = CGRectZero.readValue()) {
                    @Suppress("unused") // Called by iOS runtime
                    override fun layoutSubviews() {
                        super.layoutSubviews()

                        // Guarantee the frame update happens in the correct lifecycle hook
                        CATransaction.begin()
                        CATransaction.setValue(true, kCATransactionDisableActions)
                        previewLayer.setFrame(this.bounds)
                        CATransaction.commit()

                        Logger.d("LayoutSubviews set frame to: ${this.bounds}")
                    }
                }
                container.backgroundColor = UIColor.blackColor
                container.layer.addSublayer(previewLayer)

                Logger.i("Preview container created and layer attached")
                container
            },
            // The update block is now redundant, but we keep it for safety/orientation handling
            update = { view ->
                // This block will continue to fire, but layoutSubviews is the primary source of truth
                if (previewLayer.connection?.isVideoOrientationSupported() == true) {
                    // Update orientation here or inside layoutSubviews if needed
                    previewLayer.connection?.videoOrientation = AVCaptureVideoOrientationPortrait
                }
            },
            onRelease = { _ ->
                // Handled in DisposableEffect's onDispose
            }
        )
    }

    // Delegate to handle recording callbacks
    private class RecordingDelegate(
        val onComplete: (Boolean, NSURL?, NSError?) -> Unit
    ) : NSObject(), AVCaptureFileOutputRecordingDelegateProtocol {

        override fun captureOutput(
            output: AVCaptureFileOutput,
            didFinishRecordingToOutputFileAtURL: NSURL,
            fromConnections: List<*>,
            error: NSError?
        ) {
            // Check for both error=null AND the successful finish flag in the error userInfo
            val success = (error == null) ||
                    (error.userInfo[AVErrorRecordingSuccessfullyFinishedKey] as? Boolean == true)

            Logger.i("Recording delegate callback - success: $success, error: ${error?.code}")
            onComplete(success, didFinishRecordingToOutputFileAtURL, error)
        }
    }
}