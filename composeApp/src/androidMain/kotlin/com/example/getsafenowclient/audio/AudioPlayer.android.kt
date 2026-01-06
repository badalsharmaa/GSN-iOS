package com.example.getsafenowclient.audio

import android.media.MediaPlayer
import java.util.Collections

actual class AudioPlayerImpl : AudioPlayer {
    private var mediaPlayer: MediaPlayer? = null
    private val listeners = Collections.synchronizedList(ArrayList<PlayerListener>())
    
    private var _isPlaying = false
    actual override val isPlaying: Boolean
        get() = _isPlaying
        
    actual override val currentPosition: Long
        get() = try {
            mediaPlayer?.currentPosition?.toLong() ?: 0L
        } catch (e: Exception) { 0L }
        
    actual override val duration: Long
        get() = try {
            mediaPlayer?.duration?.toLong() ?: 0L
        } catch (e: Exception) { 0L }

    actual override fun play(url: String) {
        stop()
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(url)
                prepareAsync()
                setOnPreparedListener { 
                    start()
                    updateIsPlaying(true)
                }
                setOnCompletionListener {
                    updateIsPlaying(false)
                    notifyCompletion()
                }
                setOnErrorListener { _, _, _ ->
                    updateIsPlaying(false)
                    true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual override fun playFile(path: String) {
        play(path)
    }

    actual override fun pause() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                updateIsPlaying(false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual override fun resume() {
        try {
            if (mediaPlayer != null && !mediaPlayer!!.isPlaying) {
                mediaPlayer?.start()
                updateIsPlaying(true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual override fun stop() {
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer!!.isPlaying) {
                    mediaPlayer?.stop()
                }
                mediaPlayer?.release()
                mediaPlayer = null
            }
            updateIsPlaying(false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual override fun seekTo(positionMs: Long) {
        try {
            mediaPlayer?.seekTo(positionMs.toInt())
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
        val listenersCopy = synchronized(listeners) { ArrayList(listeners) }
        listenersCopy.forEach { it.onStateChanged(isPlaying) }
    }
    
    private fun notifyCompletion() {
        val listenersCopy = synchronized(listeners) { ArrayList(listeners) }
        listenersCopy.forEach { it.onCompletion() }
    }
}
