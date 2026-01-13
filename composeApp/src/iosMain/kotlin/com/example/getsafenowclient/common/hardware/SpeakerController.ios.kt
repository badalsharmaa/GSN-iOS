package com.example.getsafenowclient.common.hardware

import io.getsafenow.libraries.kmputils.platformkmp.ContextFactory
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryOptionAllowBluetooth
import platform.AVFAudio.AVAudioSessionCategoryOptionDefaultToSpeaker
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.setActive


actual class SpeakerController actual constructor(contextFactory: ContextFactory) {

    @OptIn(ExperimentalForeignApi::class)
    actual fun setSpeakerEnabled(enabled: Boolean) {
        val session = AVAudioSession.sharedInstance()

        // CATEGORY = PlayAndRecord (allows switching to speaker)
        val options = if (enabled) {
            AVAudioSessionCategoryOptionDefaultToSpeaker or
                    AVAudioSessionCategoryOptionAllowBluetooth
        } else {
            AVAudioSessionCategoryOptionAllowBluetooth
        }

        // Set category
        session.setCategory(
            AVAudioSessionCategoryPlayAndRecord,
            withOptions = options,
            error = null
        )

        // Activate session
        session.setActive(true, error = null)
    }
}
