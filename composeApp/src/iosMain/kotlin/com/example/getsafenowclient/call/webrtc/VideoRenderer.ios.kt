package com.example.getsafenowclient.call.webrtc

import WebRTC.RTCMTLVideoView
import WebRTC.RTCVideoTrack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import com.shepeliev.webrtckmp.MediaStream
import com.shepeliev.webrtckmp.videoTracks
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGAffineTransformMakeScale
import platform.UIKit.UIViewContentMode

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun VideoRenderer(
    stream: MediaStream,
    modifier: Modifier,
    isMirror: Boolean
) {
    // Track currently attached video track
    val currentTrack = remember { mutableStateOf<com.shepeliev.webrtckmp.VideoTrack?>(null) }

    UIKitView(
        factory = {
            RTCMTLVideoView().apply {
                contentMode = UIViewContentMode.UIViewContentModeScaleAspectFill
                clipsToBounds = true

                if (isMirror) {
                    transform = CGAffineTransformMakeScale(-1.0, 1.0)
                }
            }
        },
        update = { view ->
            val videoView = view as RTCMTLVideoView
            val newTrack = stream.videoTracks.firstOrNull()

            if (newTrack != currentTrack.value) {
                currentTrack.value?.removeRenderer(videoView)
                newTrack?.addRenderer(videoView)
                currentTrack.value = newTrack
            }
        },
        modifier = modifier,
        onRelease = { view ->
            currentTrack.value?.removeRenderer(view as RTCMTLVideoView)
            currentTrack.value = null
        }
    )
}