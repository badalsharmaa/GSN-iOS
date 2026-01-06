package com.example.getsafenowclient.common.hardware

import io.getsafenow.libraries.kmputils.platformkmp.ContextFactory

expect class SpeakerController(contextFactory: ContextFactory) {
    fun setSpeakerEnabled(enabled: Boolean)
}

expect class CallRingtoneController(contextFactory: ContextFactory) {
    fun startRingtone()
    fun startRingback()
    fun stopAllTones()
}
