package com.example.getsafenowclient.call

import io.getsafenow.libraries.kmputils.platformkmp.ContextFactory

expect class CallBackgroundManager(contextFactory: ContextFactory) {
    fun startBackgroundExecution()
    fun stopBackgroundExecution()
    fun requestAudioFocus()
    fun releaseAudioFocus()
}
