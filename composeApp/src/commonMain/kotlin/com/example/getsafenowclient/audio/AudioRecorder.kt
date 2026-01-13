package com.example.getsafenowclient.audio

import io.getsafenow.libraries.kmputils.platformkmp.PlatformFile
import kotlinx.coroutines.flow.Flow

interface AudioRecorder {
    /**
     * Starts recording audio to the specified file.
     * @param outputFile The file to write audio data to.
     */
    fun start(outputFile: PlatformFile)

    /**
     * Stops the recording and finalizes the file.
     */
    fun stop()

    /**
     * Cancels the recording and deletes the file if possible.
     */
    fun cancel()

    /**
     * Returns true if currently recording.
     */
    fun isRecording(): Boolean

    /**
     * Returns the current amplitude (volume level) of the input.
     * Normalized between 0.0 and 1.0 usually, or raw values depending on impl.
     * Useful for visualizers.
     */
    fun getMaxAmplitude(): Float
}
