package com.example.getsafenowclient.audio

import android.media.MediaRecorder
import android.os.Build
import io.getsafenow.libraries.kmputils.platformkmp.PlatformFile
import java.io.IOException

actual class AudioRecorderImpl : AudioRecorder {
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false

    actual override fun start(outputFile: PlatformFile) {
        if (isRecording) return

        try {
            @Suppress("DEPRECATION")
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Ideally pass context here, but for quick implementation without DI injection of context:
                MediaRecorder() 
            } else {
                MediaRecorder()
            }

            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile.path)
                prepare()
                start()
            }
            isRecording = true
        } catch (e: IOException) {
            e.printStackTrace()
            isRecording = false
        } catch (e: RuntimeException) {
             e.printStackTrace()
             isRecording = false
        }
    }

    actual override fun stop() {
        if (!isRecording) return
        try {
            mediaRecorder?.stop()
        } catch (e: RuntimeException) {
            e.printStackTrace()
        } finally {
            mediaRecorder?.release()
            mediaRecorder = null
            isRecording = false
        }
    }

    actual override fun cancel() {
        stop()
    }

    actual override fun isRecording(): Boolean = isRecording

    actual override fun getMaxAmplitude(): Float {
        return try {
            val amp = mediaRecorder?.maxAmplitude ?: 0
            // Normalize 0..32767 to 0..1
            amp / 32767f
        } catch (e: Exception) {
            0f
        }
    }
}
