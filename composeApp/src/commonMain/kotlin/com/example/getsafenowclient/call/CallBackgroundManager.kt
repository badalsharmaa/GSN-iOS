package com.example.getsafenowclient.call

import io.getsafenow.libraries.kmputils.platformkmp.ContextFactory

expect class CallBackgroundManager(contextFactory: ContextFactory) {
    fun startBackgroundExecution(isIncoming: Boolean = false, callerName: String? = null)
    fun stopBackgroundExecution()
    fun requestAudioFocus()
    fun releaseAudioFocus()

    var onAnswer: (() -> Unit)?
    var onHangup: (() -> Unit)?
}
