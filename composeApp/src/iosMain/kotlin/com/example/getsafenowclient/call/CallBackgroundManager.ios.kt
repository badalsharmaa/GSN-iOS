package com.example.getsafenowclient.call

import io.getsafenow.libraries.kmputils.platformkmp.ContextFactory
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.AVAudioSessionModeVoiceChat
import platform.AVFAudio.setActive

actual class CallBackgroundManager actual constructor(
    contextFactory: ContextFactory
) {
    // Use the shared controller created in MainViewController
    private val callKitController = SharedCallManager.controller

    actual fun startBackgroundExecution(isIncoming: Boolean, callerName: String?) {
        if (isIncoming && callKitController != null) {
            val handle = callerName ?: "Unknown"
            val uuid = platform.Foundation.NSUUID() 
            callKitController?.reportIncomingCall(uuid, handle, hasVideo = true) 
        }
    }

    actual fun stopBackgroundExecution() {
        // No-op for now, CallKit ends via user action usually
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun requestAudioFocus() {
        val session = AVAudioSession.sharedInstance()
        try {
            session.setCategory(AVAudioSessionCategoryPlayAndRecord, mode = AVAudioSessionModeVoiceChat, options = 0u, error = null)
            session.setActive(true, error = null)
        } catch (e: Exception) {
            // Log error
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun releaseAudioFocus() {
        val session = AVAudioSession.sharedInstance()
        try {
            session.setActive(false, error = null)
        } catch (e: Exception) {
            // Log error
        }
    }

    actual var onAnswer: (() -> Unit)?
        get() = SharedCallManager.onAnswerCallback
        set(value) { SharedCallManager.onAnswerCallback = value }

    actual var onHangup: (() -> Unit)?
        get() = SharedCallManager.onHangupCallback
        set(value) { SharedCallManager.onHangupCallback = value }
}
