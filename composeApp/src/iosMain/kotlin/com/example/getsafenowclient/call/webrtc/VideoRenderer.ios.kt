package com.example.getsafenowclient.call.webrtc

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import com.shepeliev.webrtckmp.MediaStream
import com.shepeliev.webrtckmp.videoTracks
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIView
import platform.UIKit.backgroundColor
import platform.UIKit.UIColor

// Imports for WebRTC Framework
// Note: Depending on how the pod is exposed, imports might be:
// import cocoapods.WebRTC.RTCVideoRenderer
// import cocoapods.WebRTC.RTCMTLVideoView
// import cocoapods.WebRTC.RTCVideoTrack

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun VideoRenderer(
    stream: MediaStream,
    modifier: Modifier,
    isMirror: Boolean
) {
    // We retain the view to avoid recreation on recomposition if not needed
    // However, UIKitView handles simple updates.

    UIKitView(
        factory = {
            // Create a native RTCMTLVideoView (Metal Renderer)
            // Using reflection or factory pattern if direct constructor unavailable, 
            // but typical binding usage:
            val videoView = cocoapods.WebRTC.RTCMTLVideoView()
            videoView.videoContentMode = cocoapods.WebRTC.RTCVideoContentMode.RTCVideoContentModeAspectFill
            if (isMirror) {
                // Mirroring is usually done via transform on the view or setup on the camera
                // RTCMTLVideoView usually mirrors automatically for local if configured?
                // Or we apply CGAffineTransformMakeScale(-1.0, 1.0)
                 val transform = platform.CoreGraphics.CGAffineTransformMakeScale(-1.0, 1.0)
                 videoView.transform = transform
            }
            videoView
        },
        update = { view ->
             val videoView = view as? cocoapods.WebRTC.RTCMTLVideoView ?: return@UIKitView
             
             // Attach Track
             val track = stream.videoTracks.firstOrNull()?.native as? cocoapods.WebRTC.RTCVideoTrack
             track?.addRenderer(videoView)
        },
        modifier = modifier,
        onRelease = { view ->
             val videoView = view as? cocoapods.WebRTC.RTCMTLVideoView ?: return@UIKitView
             val track = stream.videoTracks.firstOrNull()?.native as? cocoapods.WebRTC.RTCVideoTrack
             track?.removeRenderer(videoView)
        }
    )
}
