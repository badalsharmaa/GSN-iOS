package com.example.getsafenowclient.common.hardware

import io.getsafenow.libraries.kmputils.platformkmp.ContextFactory
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryOptionAllowBluetooth
import platform.AVFAudio.AVAudioSessionCategoryOptionDefaultToSpeaker
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.AVAudioSessionModeVoiceChat
import platform.AVFAudio.setActive
import platform.AudioToolbox.AudioServicesPlaySystemSound
import platform.AudioToolbox.kSystemSoundID_Vibrate
import platform.Foundation.NSBundle
import platform.Foundation.NSTimer
import platform.Foundation.NSRunLoop
import platform.Foundation.NSRunLoopCommonModes

actual class CallRingtoneController actual constructor(contextFactory: ContextFactory) {

    private var audioPlayer: AVAudioPlayer? = null
    private var hapticTimer: NSTimer? = null

    @OptIn(ExperimentalForeignApi::class)
    private fun configureAudioSession() {
        try {
            val session = AVAudioSession.sharedInstance()
            session.setCategory(
                AVAudioSessionCategoryPlayAndRecord,
                mode = AVAudioSessionModeVoiceChat,
                options = AVAudioSessionCategoryOptionDefaultToSpeaker or AVAudioSessionCategoryOptionAllowBluetooth,
                error = null
            )
            session.setActive(true, error = null)
        } catch (e: Exception) {
            println("Failed to configure AVAudioSession: $e")
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun startRingtone() {
        stopAllTones()
        configureAudioSession()

        // 1. Play Bundled Ringtone
        // Ensure you add "incoming_call.wav" (or similar) to your iOS app resources
        val path = NSBundle.mainBundle.pathForResource("incoming_call", "wav")
        if (path != null) {
            try {
                audioPlayer = AVAudioPlayer(platform.Foundation.NSURL.fileURLWithPath(path), error = null).apply {
                    numberOfLoops = -1 // Loop indefinitely
                    prepareToPlay()
                    play()
                }
            } catch (e: Exception) {
                println("AVAudioPlayer Ringtone failed: $e")
            }
        }

        // 2. Start Repeating Vibration
        hapticTimer = NSTimer.scheduledTimerWithTimeInterval(
            interval = 1.5,
            repeats = true
        ) {
            AudioServicesPlaySystemSound(kSystemSoundID_Vibrate)
        }
        NSRunLoop.mainRunLoop.addTimer(hapticTimer!!, NSRunLoopCommonModes)
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun startRingback() {
        stopAllTones()
        configureAudioSession()

        // 3. Play Bundled Ringback Tone (the "beep-beep" the caller hears)
        // Ensure you add "ringback.wav" (or similar) to your iOS app resources
        val path = NSBundle.mainBundle.pathForResource("ringback", "wav")
        if (path != null) {
            try {
                audioPlayer = AVAudioPlayer(platform.Foundation.NSURL.fileURLWithPath(path), error = null).apply {
                    numberOfLoops = -1
                    prepareToPlay()
                    play()
                }
            } catch (e: Exception) {
                println("AVAudioPlayer Ringback failed: $e")
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun stopAllTones() {
        audioPlayer?.stop()
        audioPlayer = null

        hapticTimer?.invalidate()
        hapticTimer = null

        try {
            AVAudioSession.sharedInstance().setActive(false, error = null)
        } catch (e: Exception) {
            // Ignore session deactivation errors
        }
    }
}
