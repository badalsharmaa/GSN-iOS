package com.example.getsafenowclient.call.webrtc

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.shepeliev.webrtckmp.MediaStream
import com.shepeliev.webrtckmp.videoTracks
import org.webrtc.EglBase
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer
import com.shepeliev.webrtckmp.VideoTrack

@Composable
actual fun VideoRenderer(
    stream: MediaStream,
    modifier: Modifier,
    isMirror: Boolean
) {
    var videoTrack by remember { mutableStateOf<VideoTrack?>(null) }
    
    // Use the library's root EglBase to ensure all renderers and decoders 
    // share the same OpenGL context. 
    val eglBase = com.shepeliev.webrtckmp.WebRtc.rootEglBase

    // NOTE: We do NOT release the shared EglBase here. It should live for the app lifecycle.

    AndroidView(
        factory = { ctx ->
            SurfaceViewRenderer(ctx).apply {
                init(eglBase.eglBaseContext, null)
                setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
                setEnableHardwareScaler(true)
                setMirror(isMirror)
                setZOrderMediaOverlay(isMirror)
            }
        },
        update = { view ->
            view.setMirror(isMirror)
            
            // On Android, the KMP VideoTrack interface has addSink/removeSink methods 
            // that take org.webrtc.VideoSink.
            val newTrack = stream.videoTracks.firstOrNull()
            
            if (newTrack != videoTrack) {
                videoTrack?.removeSink(view)
                newTrack?.addSink(view)
                videoTrack = newTrack
            }
        },
        modifier = modifier,
        onRelease = { view ->
            videoTrack?.removeSink(view)
            view.release()
        }
    )
}
