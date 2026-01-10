package com.example.getsafenowclient.call

import android.content.Context
import com.example.getsafenowclient.services.AudioFocusManager
import com.example.getsafenowclient.services.CallForegroundService
import io.getsafenow.libraries.kmputils.platformkmp.ContextFactory

actual class CallBackgroundManager actual constructor(
    contextFactory: ContextFactory
) {
    private val context = contextFactory.getContext() as Context
    private val audioFocusManager = AudioFocusManager(context)

    actual fun startBackgroundExecution() {
        CallForegroundService.start(context)
    }

    actual fun stopBackgroundExecution() {
        CallForegroundService.stop(context)
    }

    actual fun requestAudioFocus() {
        audioFocusManager.requestAudioFocus()
    }

    actual fun releaseAudioFocus() {
        audioFocusManager.abandonAudioFocus()
    }
}
