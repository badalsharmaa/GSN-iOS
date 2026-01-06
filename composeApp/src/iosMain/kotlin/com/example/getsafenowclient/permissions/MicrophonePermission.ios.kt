package com.example.getsafenowclient.permissions

import io.getsafenow.libraries.kmputils.platformkmp.ContextFactory
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionRecordPermissionGranted
import platform.AVFAudio.AVAudioSessionRecordPermissionUndetermined
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual class MicrophonePermissionImpl actual constructor(
    private val contextFactory: ContextFactory
) : MicrophonePermission {
    
    @OptIn(ExperimentalForeignApi::class)
    actual override fun hasPermission(): Boolean {
        val session = AVAudioSession.sharedInstance()
        return session.recordPermission == AVAudioSessionRecordPermissionGranted
    }

    @OptIn(ExperimentalForeignApi::class)
    actual override suspend fun requestPermission(): Boolean {
        val session = AVAudioSession.sharedInstance()
        return when (session.recordPermission) {
            AVAudioSessionRecordPermissionGranted -> true
            AVAudioSessionRecordPermissionUndetermined -> {
                suspendCoroutine { continuation ->
                    // Using the standard AVAudioSession API which is widely compatible.
                    // Note: The callback might be on a background thread, but suspendCoroutine handles the resumption.
                    session.requestRecordPermission { granted ->
                        continuation.resume(granted)
                    }
                }
            }
            else -> false // Denied
        }
    }

    actual override fun openSettings() {
        val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
        if (UIApplication.sharedApplication.canOpenURL(url)) {
            UIApplication.sharedApplication.openURL(url)
        }
    }
}
