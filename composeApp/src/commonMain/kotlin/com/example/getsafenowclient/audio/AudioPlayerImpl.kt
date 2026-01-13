package com.example.getsafenowclient.audio

expect class AudioPlayerImpl() : AudioPlayer {
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
    
    override fun addListener(listener: PlayerListener)
    override fun removeListener(listener: PlayerListener)
}
