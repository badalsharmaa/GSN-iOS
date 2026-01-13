package com.example.getsafenowclient.camera

import android.content.Context
import android.view.ViewGroup.LayoutParams
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import java.util.Collections

actual class VideoPlayerImpl : VideoPlayer {
    private var exoPlayer: ExoPlayer? = null
    private var playerView: PlayerView? = null // FIX: Hold reference to view
    
    private val listeners = Collections.synchronizedList(ArrayList<VideoPlayerListener>())
    
    private var _isPlaying = false
    actual override val isPlaying: Boolean
        get() = _isPlaying

    actual override val currentPosition: Long
        get() = exoPlayer?.currentPosition ?: 0L

    actual override val duration: Long
        get() = exoPlayer?.duration ?: 0L
    
    private var pendingUrl: String? = null

    actual override fun play(url: String) {
        pendingUrl = url
        exoPlayer?.let { player ->
            val mediaItem = MediaItem.fromUri(url)
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
        }
    }

    actual override fun playFile(path: String) {
        play(path)
    }

    actual override fun pause() {
        exoPlayer?.pause()
    }

    actual override fun resume() {
        exoPlayer?.play()
    }

    actual override fun stop() {
        exoPlayer?.stop()
    }

    actual override fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
    }

    actual override fun release() {
        // FIX: Detach player from view to release Surface
        playerView?.player = null
        playerView = null
        
        exoPlayer?.release()
        exoPlayer = null
    }

    actual override fun addListener(listener: VideoPlayerListener) {
        listeners.add(listener)
    }

    actual override fun removeListener(listener: VideoPlayerListener) {
        listeners.remove(listener)
    }

    private fun initPlayer(context: Context) {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context).build().apply {
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        _isPlaying = isPlaying
                        notifyStateChanged(isPlaying)
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_ENDED) {
                            notifyCompletion()
                        }
                    }
                })
            }
            
            // FIX: Attach to view if available
            playerView?.player = exoPlayer

            pendingUrl?.let { play(it) }
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

    @OptIn(UnstableApi::class)
    @Composable
    actual override fun VideoView(modifier: Modifier) {
        val context = LocalContext.current
        
        DisposableEffect(context) {
            initPlayer(context)
            onDispose {
                // stop() is usually enough, release is called by parent
                stop()
            }
        }

        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    layoutParams = FrameLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT, 
                        LayoutParams.MATCH_PARENT
                    )
                    
                    // FIX: Capture view and set player
                    this@VideoPlayerImpl.playerView = this
                    this.player = this@VideoPlayerImpl.exoPlayer
                }
            },
            modifier = modifier
        )
    }
}
