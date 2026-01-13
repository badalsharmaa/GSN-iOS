package com.example.getsafenowclient.camera

import io.getsafenow.libraries.kmputils.platformkmp.PlatformFile

expect class VideoRecorderImpl() : VideoRecorder {
    override fun start(outputFile: PlatformFile, onRecordingFinished: (thumbnail: PlatformFile?, error: Throwable?) -> Unit)
    override fun stop()
    override fun cancel()
    override fun switchCamera()
    override fun isRecording(): Boolean
    override fun isReady(): Boolean
    override fun release()

    @androidx.compose.runtime.Composable
    override fun CameraPreview(modifier: androidx.compose.ui.Modifier)
}
