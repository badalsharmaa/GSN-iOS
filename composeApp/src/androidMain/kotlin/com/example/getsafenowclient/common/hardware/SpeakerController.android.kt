package com.example.getsafenowclient.common.hardware

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import androidx.annotation.RequiresApi
import io.getsafenow.libraries.kmputils.platformkmp.ContextFactory

actual class SpeakerController actual constructor(
    contextFactory: ContextFactory
) {

    private val context = contextFactory.getContext() as Context

    @SuppressLint("ServiceCast")
    private val audioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    actual fun setSpeakerEnabled(enabled: Boolean) {
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            setSpeakerApi31(enabled)
        } else {
            @Suppress("DEPRECATION")
            audioManager.isSpeakerphoneOn = enabled
        }
    }

    // Android 12+
    @RequiresApi(Build.VERSION_CODES.S)
    private fun setSpeakerApi31(enabled: Boolean) {
        val devices = audioManager.availableCommunicationDevices

        val targetType = if (enabled) {
            AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
        } else {
            AudioDeviceInfo.TYPE_BUILTIN_EARPIECE
        }

        val device = devices.firstOrNull { it.type == targetType }

        if (device != null) {
            audioManager.setCommunicationDevice(device)
        }
    }
}

