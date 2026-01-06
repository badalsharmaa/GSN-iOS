package com.example.getsafenowclient.call

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.getsafenowclient.component.call.CallOverlayBanner
import com.example.getsafenowclient.component.call.CallScreen
import com.example.getsafenowclient.component.call.CallVideoLayout
import net.folivo.trixnity.client.MatrixClient

@Composable
fun CallScreen(
    component: CallScreenModel,
    client: MatrixClient,
    modifier: Modifier = Modifier
) {
    val state by component.state.collectAsState()
    val localStream by component.webrtc.localStream.collectAsState()
    val remoteStream by component.webrtc.remoteStream.collectAsState()

    // 1. Minimized Banner Mode
    if (state.isMinimized) {
        CallOverlayBanner(
            state = state,
            client = client,
            onExpandClick = { component.dispatch(CallEvent.Restore) },
            onEndCallClick = { component.dispatch(CallEvent.Hangup) },
            onAcceptClick = { component.dispatch(CallEvent.AcceptCall) },
            onToggleMic = { component.dispatch(CallEvent.ToggleMic) },
            onToggleSpeaker = { component.dispatch(CallEvent.ToggleSpeaker) },
            modifier = modifier
        )
        return
    }

    // 2. Full Screen Mode
    if (state.callState is CallState.Idle && state.callState !is CallState.Ended) {
        // No active call UI to show if idle (and not just ended showing summary)
        return
    }

    // Video Content Composable (passed only if it is a video call)
    val videoContent: (@Composable () -> Unit)? = if (state.isVideoCall) {
        {
            CallVideoLayout(
                remoteVideo = {
                    // TODO: Implement actual VideoRenderer for KMP (e.g. SurfaceViewRenderer)
                    // For now, these are placeholders or platform-specific expect/actuals
                    // remoteStream?.let { VideoRenderer(it, isMirror = false) }
                },
                localVideo = if (state.isVideoEnabled) {
                    {
                        // localStream?.let { VideoRenderer(it, isMirror = true) }
                    }
                } else null
            )
        }
    } else null

    CallScreen(
        state = state,
        client = client,
        onMinimizeClick = { component.dispatch(CallEvent.Minimize) },
        onEndCallClick = { component.dispatch(CallEvent.Hangup) },
        onAcceptClick = { component.dispatch(CallEvent.AcceptCall) },
        onToggleMic = { component.dispatch(CallEvent.ToggleMic) },
        onToggleCamera = { component.dispatch(CallEvent.ToggleCamera) },
        onToggleSpeaker = { component.dispatch(CallEvent.ToggleSpeaker) },
        onSwitchCamera = { component.dispatch(CallEvent.SwitchCamera) },
        videoContent = videoContent,
        modifier = modifier
    )
}
