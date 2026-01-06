package com.example.getsafenowclient.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

expect class VideoPlayerImpl() : VideoPlayer {
    override val isPlaying: Boolean
    override val currentPosition: Long
    override val duration: Long
    
    override fun play(url: String)
    override fun playFile(path: String)
    override fun pause()
    override fun resume()
    override fun stop()
    override fun seekTo(positionMs: Long)
    override fun release()
    
    override fun addListener(listener: VideoPlayerListener)
    override fun removeListener(listener: VideoPlayerListener)

    @Composable
    override fun VideoView(modifier: Modifier)
}
