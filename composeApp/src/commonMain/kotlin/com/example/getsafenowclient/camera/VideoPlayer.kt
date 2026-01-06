package com.example.getsafenowclient.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface VideoPlayer {
    val isPlaying: Boolean
    val currentPosition: Long
    val duration: Long
    
    /**
     * Play video from the given URL.
     */
    fun play(url: String)
    
    /**
     * Play video from a local file path.
     */
    fun playFile(path: String)
    
    fun pause()
    fun resume()
    fun stop()
    fun seekTo(positionMs: Long)
    fun release()
    
    fun addListener(listener: VideoPlayerListener)
    fun removeListener(listener: VideoPlayerListener)

    /**
     * Renders the video content.
     * This must be placed in the UI to see the video.
     */
    @Composable
    fun VideoView(modifier: Modifier)
}

interface VideoPlayerListener {
    fun onProgress(positionMs: Long)
    fun onCompletion()
    fun onStateChanged(isPlaying: Boolean)
}
