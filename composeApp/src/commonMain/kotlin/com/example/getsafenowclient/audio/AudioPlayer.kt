package com.example.getsafenowclient.audio

import kotlinx.coroutines.flow.StateFlow

interface AudioPlayer {
    val isPlaying: Boolean
    val currentPosition: Long
    val duration: Long
    
    /**
     * Play audio from the given URL.
     */
    fun play(url: String)
    
    /**
     * Play audio from a local file path.
     */
    fun playFile(path: String)
    
    fun pause()
    fun resume()
    fun stop()
    fun seekTo(positionMs: Long)
    fun release()
    
    fun addListener(listener: PlayerListener)
    fun removeListener(listener: PlayerListener)
}

interface PlayerListener {
    fun onProgress(positionMs: Long)
    fun onCompletion()
    fun onStateChanged(isPlaying: Boolean)
}
