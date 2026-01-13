package com.example.getsafenowclient.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.getsafenow.libraries.kmputils.platformkmp.PlatformFile

interface VideoRecorder {
    /**
     * Starts recording video to the specified file.
     * @param onRecordingFinished Callback invoked when recording is finalized (after stop).
     *                            Passes the thumbnail file (if successful) and/or an exception on error.
     */
    fun start(outputFile: PlatformFile, onRecordingFinished: (thumbnail: PlatformFile?, error: Throwable?) -> Unit)

    /**
     * Stops the recording and finalizes the file.
     */
    fun stop()

    /**
     * Cancels the recording (stop and delete).
     */
    fun cancel()

    /**
     * Toggles between front and back cameras.
     */
    fun switchCamera()

    /**
     * Returns true if currently recording.
     */
    fun isRecording(): Boolean

    /**
     * Returns true if the recorder is ready to start (e.g. camera initialized).
     */
    fun isReady(): Boolean

    /**
     * Releases resources (camera, session, etc.).
     */
    fun release()

    /**
     * Renders the camera preview.
     * This Composable must be present in the UI for the recorder to work (usually).
     */
    @Composable
    fun CameraPreview(modifier: Modifier)
}
