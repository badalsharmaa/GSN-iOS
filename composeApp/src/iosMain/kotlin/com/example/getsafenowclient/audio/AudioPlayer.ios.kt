package com.example.getsafenowclient.audio

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioPlayerDelegateProtocol
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.Foundation.NSURL
import platform.darwin.NSObject

actual class AudioPlayerImpl : AudioPlayer {
    private var audioPlayer: AVAudioPlayer? = null
    private val delegate = PlayerDelegate()
    private val listeners = mutableListOf<PlayerListener>()
    
    private var _isPlaying = false
    actual override val isPlaying: Boolean
        get() = _isPlaying
        
    actual override val currentPosition: Long
        get() = (audioPlayer?.currentTime ?: 0.0).toLong() * 1000
        
    actual override val duration: Long
        get() = (audioPlayer?.duration ?: 0.0).toLong() * 1000

    @OptIn(ExperimentalForeignApi::class)
    actual override fun play(url: String) {
        // Streaming from URL is complex with AVAudioPlayer (it prefers files).
        // For a real app, you'd use AVPlayer.
        // Since we are likely playing downloaded files or cached files, let's assume file path for now
        // or implement AVPlayer if streaming is needed.
        // Given the constraints, let's try to treat it as file url.
        val nsUrl = NSURL.URLWithString(url) ?: return
        playUrl(nsUrl)
    }

    actual override fun playFile(path: String) {
        val nsUrl = NSURL.fileURLWithPath(path)
        playUrl(nsUrl)
    }
    
    @OptIn(ExperimentalForeignApi::class)
    private fun playUrl(url: NSURL) {
        stop()
        try {
            val session = AVAudioSession.sharedInstance()
            session.setCategory(AVAudioSessionCategoryPlayback, error = null)
            session.setActive(true, error = null)
            
            audioPlayer = AVAudioPlayer(contentsOfURL = url, error = null).apply {
                delegate = this@AudioPlayerImpl.delegate
                prepareToPlay()
                play()
            }
            updateIsPlaying(true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual override fun pause() {
        if (audioPlayer?.playing == true) {
            audioPlayer?.pause()
            updateIsPlaying(false)
        }
    }

    actual override fun resume() {
        if (audioPlayer != null && !audioPlayer!!.playing) {
            audioPlayer?.play()
            updateIsPlaying(true)
        }
    }

    actual override fun stop() {
        if (audioPlayer?.playing == true) {
            audioPlayer?.stop()
        }
        audioPlayer = null
        updateIsPlaying(false)
    }

    actual override fun seekTo(positionMs: Long) {
        audioPlayer?.currentTime = positionMs / 1000.0
    }

    actual override fun release() {
        stop()
        listeners.clear()
    }

    actual override fun addListener(listener: PlayerListener) {
        listeners.add(listener)
    }

    actual override fun removeListener(listener: PlayerListener) {
        listeners.remove(listener)
    }
    
    private fun updateIsPlaying(value: Boolean) {
        if (_isPlaying != value) {
            _isPlaying = value
            notifyStateChanged(value)
        }
    }
    
    private fun notifyStateChanged(isPlaying: Boolean) {
        // Copy list to avoid ConcurrentModificationException
        listeners.toList().forEach { it.onStateChanged(isPlaying) }
    }
    
    private fun notifyCompletion() {
        // Copy list to avoid ConcurrentModificationException
        listeners.toList().forEach { it.onCompletion() }
    }
    
    private inner class PlayerDelegate : NSObject(), AVAudioPlayerDelegateProtocol {
        override fun audioPlayerDidFinishPlaying(player: AVAudioPlayer, successfully: Boolean) {
            updateIsPlaying(false)
            notifyCompletion()
        }
    }
}
