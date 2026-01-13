package com.example.getsafenowclient.audio

import io.getsafenow.libraries.kmputils.platformkmp.PlatformFile
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioQualityMedium
import platform.AVFAudio.AVAudioRecorder
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.AVAudioSessionModeDefault
import platform.AVFAudio.setActive
import platform.Foundation.NSURL
import platform.Foundation.setValue
import platform.AVFAudio.AVFormatIDKey
import platform.AVFAudio.AVSampleRateKey
import platform.AVFAudio.AVNumberOfChannelsKey
import platform.AVFAudio.AVEncoderAudioQualityKey
import platform.CoreAudioTypes.kAudioFormatMPEG4AAC

actual class AudioRecorderImpl : AudioRecorder {
    private var recorder: AVAudioRecorder? = null
    private var isRecording = false

    @OptIn(ExperimentalForeignApi::class)
    actual override fun start(outputFile: PlatformFile) {
        if (isRecording) return

        val session = AVAudioSession.sharedInstance()
        try {
            session.setCategory(AVAudioSessionCategoryPlayAndRecord, error = null)
            session.setMode(AVAudioSessionModeDefault, error = null)
            session.setActive(true, error = null)

            val settings = mapOf<Any?, Any?>(
                AVFormatIDKey to kAudioFormatMPEG4AAC,
                AVSampleRateKey to 44100.0,
                AVNumberOfChannelsKey to 1,
                AVEncoderAudioQualityKey to AVAudioQualityMedium
            )

            val url = NSURL.fileURLWithPath(outputFile.path)
            recorder = AVAudioRecorder(url, settings, null)
            recorder?.prepareToRecord()
            recorder?.meteringEnabled = true
            recorder?.record()
            isRecording = true

        } catch (e: Exception) {
            e.printStackTrace()
            isRecording = false
        }
    }

    actual override fun stop() {
        if (!isRecording) return
        recorder?.stop()
        recorder = null
        isRecording = false
        
        // Deactivate session (optional, depending on if you want to keep audio active)
        // AVAudioSession.sharedInstance().setActive(false, error = null)
    }

    actual override fun cancel() {
        stop()
        // Logic to delete the file is usually handled by the caller via the outputFile they passed
    }

    actual override fun isRecording(): Boolean = isRecording

    actual override fun getMaxAmplitude(): Float {
        recorder?.updateMeters()
        // averagePowerForChannel returns decibels (dB), usually -160 to 0.
        // We want to normalize this to 0..1
        val power = recorder?.averagePowerForChannel(0u) ?: -160f
        // Simple normalization: map -60dB..0dB to 0..1
        return if (power < -60f) 0f else (power + 60f) / 60f
    }
}
