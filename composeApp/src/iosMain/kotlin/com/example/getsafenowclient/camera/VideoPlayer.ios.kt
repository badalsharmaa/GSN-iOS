package com.example.getsafenowclient.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import co.touchlab.kermit.Logger
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import kotlinx.cinterop.useContents
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
import platform.AVFoundation.AVPlayerLayer
import platform.AVFoundation.addPeriodicTimeObserverForInterval
import platform.AVFoundation.currentItem
import platform.AVFoundation.currentTime
import platform.AVFoundation.duration
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.removeTimeObserver
import platform.AVFoundation.replaceCurrentItemWithPlayerItem
import platform.AVFoundation.seekToTime
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.CoreGraphics.CGRectZero
import platform.CoreMedia.CMTimeMake
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSURL
import platform.UIKit.UIView

actual class VideoPlayerImpl : VideoPlayer {
    private val player = AVPlayer()
    private val listeners = mutableListOf<VideoPlayerListener>()

    private var _isPlaying = false
    actual override val isPlaying: Boolean
        get() = _isPlaying

    @OptIn(ExperimentalForeignApi::class)
    actual override val currentPosition: Long
        get() {
            val time = player.currentTime()
            return time.useContents {
                if (timescale == 0) 0L else (value * 1000 / timescale).toLong()
            }
        }

    @OptIn(ExperimentalForeignApi::class)
    actual override val duration: Long
        get() {
            val item = player.currentItem ?: return 0L
            val time = item.duration
            return time.useContents {
                if (timescale == 0) 0L else (value * 1000 / timescale).toLong()
            }
        }

    private var timeObserverToken: Any? = null
    private var completionObserver: Any? = null

    init {
        setupAudioSession()
        setupTimeObserver()
        setupCompletionObserver()
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun setupAudioSession() {
        try {
            val session = AVAudioSession.sharedInstance()
            session.setCategory(AVAudioSessionCategoryPlayback, error = null)
            session.setActive(true, error = null)
        } catch (e: Exception) {
            Logger.e("Error setting up audio session: ${e.message}")
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun setupTimeObserver() {
        val interval = CMTimeMake(1, 10) // 100ms
        timeObserverToken = player.addPeriodicTimeObserverForInterval(interval, null) { time ->
            val pos = time.useContents {
                if (timescale == 0) 0L else (value * 1000 / timescale)
            }
            notifyProgress(pos)
        }
    }

    private fun setupCompletionObserver() {
        completionObserver = NSNotificationCenter.defaultCenter.addObserverForName(
            name = AVPlayerItemDidPlayToEndTimeNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue
        ) { notification ->
            val currentItem = player.currentItem
            if (currentItem != null && notification?.`object` == currentItem) {
                notifyCompletion()
                pause() // Or stop, depending on desired behavior
                seekTo(0)
                _isPlaying = false
                notifyStateChanged(false)
            }
        }
    }

    actual override fun play(url: String) {
        val nsUrl = NSURL.URLWithString(url) ?: return
        val item = AVPlayerItem(uRL = nsUrl)
        playItem(item)
    }

    actual override fun playFile(path: String) {
        val nsUrl = NSURL.fileURLWithPath(path)
        val item = AVPlayerItem(uRL = nsUrl)
        playItem(item)
    }

    private fun playItem(item: AVPlayerItem) {
        player.replaceCurrentItemWithPlayerItem(item)
        player.play()
        _isPlaying = true
        notifyStateChanged(true)
    }

    actual override fun pause() {
        player.pause()
        _isPlaying = false
        notifyStateChanged(false)
    }

    actual override fun resume() {
        player.play()
        _isPlaying = true
        notifyStateChanged(true)
    }

    actual override fun stop() {
        player.pause()
        player.replaceCurrentItemWithPlayerItem(null)
        _isPlaying = false
        notifyStateChanged(false)
    }

    @OptIn(ExperimentalForeignApi::class)
    actual override fun seekTo(positionMs: Long) {
        val time = CMTimeMake(positionMs, 1000)
        player.seekToTime(time)
    }

    actual override fun release() {
        stop()
        timeObserverToken?.let { player.removeTimeObserver(it) }
        timeObserverToken = null
        
        completionObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
        completionObserver = null
        
        listeners.clear()
    }

    actual override fun addListener(listener: VideoPlayerListener) {
        listeners.add(listener)
    }

    actual override fun removeListener(listener: VideoPlayerListener) {
        listeners.remove(listener)
    }

    private fun notifyProgress(pos: Long) {
        listeners.forEach { it.onProgress(pos) }
    }

    private fun notifyStateChanged(isPlaying: Boolean) {
        listeners.forEach { it.onStateChanged(isPlaying) }
    }

    private fun notifyCompletion() {
        listeners.forEach { it.onCompletion() }
    }

    @OptIn(ExperimentalForeignApi::class)
    @Composable
    actual override fun VideoView(modifier: Modifier) {
        val playerLayer = remember { AVPlayerLayer.playerLayerWithPlayer(player) }

        // Manage lifecycle
        DisposableEffect(Unit) {
            onDispose {
                pause() // Pause playback when the composable leaves the screen
            }
        }

        UIKitView(
            modifier = modifier,
            factory = {
                // *** FIX: Use custom UIView to guarantee frame update in layoutSubviews ***
                val container = object : UIView(frame = CGRectZero.readValue()) {
                    @Suppress("unused") // Called by iOS runtime
                    override fun layoutSubviews() {
                        super.layoutSubviews()

                        // Set the playerLayer frame to match the container's bounds
                        // every time the view is resized.
                        playerLayer.setFrame(this.bounds)
                        Logger.d("AVPlayerLayer frame set to: ${this.bounds}")
                    }
                }

                playerLayer.videoGravity = AVLayerVideoGravityResizeAspectFill
                container.layer.addSublayer(playerLayer)

                container
            },
            update = {
                // The update block can still handle additional connection/orientation settings if needed,
                // but sizing is now reliably handled by layoutSubviews.
            }
        )
    }
}
