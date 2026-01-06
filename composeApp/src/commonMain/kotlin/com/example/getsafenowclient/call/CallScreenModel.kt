package com.example.getsafenowclient.call

import com.example.getsafenowclient.call.webrtc.WebRtcManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.client.store.RoomUser
import net.folivo.trixnity.client.store.avatarUrl
import net.folivo.trixnity.client.user
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.UserId
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Global Call Screen Model.
 * Manages call state across the entire application.
 */
class CallScreenModel(
    val webrtc: WebRtcManager,
    private val signaling: CallSignalingHandler,
    private val scope: CoroutineScope,
    private val clientProvider: () -> MatrixClient
) {

    private val _state = MutableStateFlow(CallUiState())
    val state: StateFlow<CallUiState> = _state.asStateFlow()

    init {
        observeWebRtcCallbacks()
    }

    fun dispatch(event: CallEvent) {
        scope.launch { reduce(event) }
    }

    fun reset() {
        webrtc.ringtoneController?.stopAllTones()
        signaling.clearCurrentCall()
        webrtc.close()
        _state.value = CallUiState()
    }

    // ---------------------------------------------------------
    // OUTGOING CALL ENTRY POINT
    // ---------------------------------------------------------
    suspend fun startOutgoingCall(roomId: RoomId, isVideo: Boolean, opponentId: String) {
        // Resolve Peer Info immediately
        updatePeerInfo(roomId, opponentId)

        val newCallId = signaling.startOutgoingCall(roomId)

        // 🎵 Start Ringback
        webrtc.ringtoneController?.startRingback()

        // Prepare local media
        webrtc.setupLocalStream(video = isVideo, audio = true)

        // Begin offer creation
        webrtc.startCall(isVideo)

        // Update UI
        _state.update {
            it.copy(
                roomId = roomId.full,
                peerId = opponentId,
                isVideoCall = isVideo,
                isVideoEnabled = isVideo,
                callState = CallState.OutgoingRinging(
                    callId = newCallId,
                    opponentId = opponentId
                )
            )
        }
    }

    // ---------------------------------------------------------
    // REDUCER (Handles all CallEvents)
    // ---------------------------------------------------------
    private suspend fun reduce(event: CallEvent) {
        val current = _state.value

        when (event) {
            is CallEvent.ToggleMic -> toggleMic(current)
            is CallEvent.ToggleCamera -> toggleCamera(current)
            is CallEvent.ToggleSpeaker -> toggleSpeaker(current)
            is CallEvent.SwitchCamera -> webrtc.switchCamera()

            is CallEvent.Minimize -> _state.update { it.copy(isMinimized = true) }
            is CallEvent.Restore -> _state.update { it.copy(isMinimized = false) }

            is CallEvent.Hangup -> hangupCall()
            is CallEvent.AcceptCall -> acceptIncomingCall(current)
            is CallEvent.RejectCall -> rejectIncomingCall(current)

            // ➤ MATRIX SIGNALING EVENTS (Now including RoomId)
            is CallEvent.IncomingInvite -> onIncomingInvite(event)
            is CallEvent.RemoteAnswered -> onRemoteAnswered(event)
            is CallEvent.RemoteIceCandidate ->
                webrtc.handleRemoteCandidate(event.sdp, event.sdpMid, event.sdpMLineIndex)

            is CallEvent.RemoteHangup -> onRemoteHangup(event)

            // ➤ WEBRTC INTERNAL EVENTS
            CallEvent.WebRtcConnected -> onWebRtcConnected(current)
            CallEvent.WebRtcDisconnected -> endCall(EndCallReason.ConnectionDropped)

            is CallEvent.LocalCameraFailed -> disableVideoFallback()
            CallEvent.LocalVideoStopped -> disableVideoFallback()
            CallEvent.RemoteVideoStopped -> _state.update { it.copy(isVideoEnabled = false) }

            CallEvent.IceFailed -> endCall(EndCallReason.IceFailed)
            CallEvent.IceTimeout -> endCall(EndCallReason.IceFailed)
        }
    }

    // ---------------------------------------------------------
    // USER ACTION HANDLERS
    // ---------------------------------------------------------
    private fun toggleMic(current: CallUiState) {
        val enabled = !current.isMicEnabled
        _state.update { it.copy(isMicEnabled = enabled) }
        webrtc.setMicrophoneEnabled(enabled)
    }

    private fun toggleCamera(current: CallUiState) {
        if (!current.isVideoCall) return
        val newEnabled = !current.isVideoEnabled
        _state.update { it.copy(isVideoEnabled = newEnabled) }
        webrtc.setCameraEnabled(newEnabled)
    }

    private fun toggleSpeaker(current: CallUiState) {
        val enabled = !current.isSpeakerEnabled
        _state.update { it.copy(isSpeakerEnabled = enabled) }
        webrtc.setSpeakerEnabled(enabled)
    }

    private suspend fun hangupCall() {
        signaling.sendHangup()
        endCall(EndCallReason.LocalHangup)
    }

    private fun acceptIncomingCall(current: CallUiState) {
        val st = current.callState
        if (st !is CallState.IncomingRinging) return

        webrtc.ringtoneController?.stopAllTones()

        webrtc.acceptCall(st.offerSdp, current.isVideoCall)
        signaling.notifyAnswerGenerated()

        _state.update {
            it.copy(callState = CallState.Connecting(st.callId, st.opponentId))
        }
    }

    private suspend fun rejectIncomingCall(current: CallUiState) {
        val st = current.callState
        if (st !is CallState.IncomingRinging) return
        val rid = current.roomId?.let { RoomId(it) } ?: return

        webrtc.ringtoneController?.stopAllTones()

        signaling.sendReject(rid, st.callId)
        endCall(EndCallReason.Rejected)
    }

    // --------------------------------------------------------
    // SIGNALING HANDLERS
    // ---------------------------------------------------------
    private fun onIncomingInvite(e: CallEvent.IncomingInvite) {
    }
    
    // Internal version of onIncomingInvite that accepts RoomId
    fun handleIncomingInvite(e: CallEvent.IncomingInvite, roomId: RoomId) {
        if (_state.value.callState !is CallState.Idle) {
            scope.launch { signaling.sendBusy(roomId, e.callId) }
            return
        }
        
        webrtc.ringtoneController?.startRingtone()
        updatePeerInfo(roomId, e.opponentId)

        scope.launch {
            webrtc.setupLocalStream(video = e.isVideo, audio = true)
        }

        _state.update {
            it.copy(
                roomId = roomId.full,
                peerId = e.opponentId,
                isVideoCall = e.isVideo,
                isVideoEnabled = e.isVideo,
                callState = CallState.IncomingRinging(
                    callId = e.callId,
                    opponentId = e.opponentId,
                    offerSdp = e.offerSdp
                )
            )
        }
    }

    private fun onRemoteAnswered(e: CallEvent.RemoteAnswered) {
        webrtc.handleRemoteAnswer(e.answerSdp)

        _state.update {
            val prev = it.callState
            if (prev is CallState.OutgoingRinging)
                it.copy(callState = CallState.Connecting(prev.callId, prev.opponentId))
            else it
        }
    }

    private fun onRemoteHangup(e: CallEvent.RemoteHangup) {
        endCall(e.reason)
    }

    @OptIn(ExperimentalTime::class)
    private fun onWebRtcConnected(current: CallUiState) {
        webrtc.ringtoneController?.stopAllTones()

        val st = current.callState
        if (st is CallState.Connecting) {
            _state.update {
                it.copy(
                    callState = CallState.InCall(
                        callId = st.callId,
                        opponentId = st.opponentId,
                        startTimestamp = Clock.System.now().toEpochMilliseconds()
                    )
                )
            }
            // 🎤 FIX FOR iOS INITIAL MUTE:
            // Re-assert the microphone state after connection is established.
            // This forces the iOS audio session to properly route the microphone input.
            webrtc.setMicrophoneEnabled(current.isMicEnabled)
        }
    }

    private fun disableVideoFallback() {
        _state.update { it.copy(isVideoEnabled = false) }
        webrtc.setCameraEnabled(false)
    }

    private fun endCall(reason: EndCallReason) {
        webrtc.ringtoneController?.stopAllTones()
        signaling.clearCurrentCall()
        webrtc.close()

        _state.update {
            val callId = when (val st = it.callState) {
                is CallState.InCall -> st.callId
                is CallState.Connecting -> st.callId
                is CallState.OutgoingRinging -> st.callId
                is CallState.IncomingRinging -> st.callId
                else -> null
            }

            it.copy(
                callState = CallState.Ended(callId, reason),
                isVideoCall = false,
                isVideoEnabled = false,
                isMicEnabled = true,
                isSpeakerEnabled = false
            )
        }

        scope.launch {
            delay(1500)
            _state.value = CallUiState()
        }
    }

    private fun observeWebRtcCallbacks() {
        webrtc.onConnected = { dispatch(CallEvent.WebRtcConnected) }
        webrtc.onDisconnected = { dispatch(CallEvent.WebRtcDisconnected) }
        webrtc.onCameraFailure = { dispatch(CallEvent.LocalCameraFailed(it)) }
        webrtc.onRemoteVideoStopped = { dispatch(CallEvent.RemoteVideoStopped) }
        webrtc.onLocalVideoStopped = { dispatch(CallEvent.LocalVideoStopped) }
    }

    fun resumeIncomingCall(
        callId: String,
        isVideo: Boolean,
        isIncoming: Boolean
    ) {
        val current = _state.value
        if (current.callState is CallState.IncomingRinging ||
            current.callState is CallState.Connecting ||
            current.callState is CallState.InCall
        ) {
            _state.update { it.copy(isMinimized = false) }
        }
    }
    
    private fun updatePeerInfo(roomId: RoomId, userIdFull: String?) {
        if (userIdFull.isNullOrBlank()) return
        _state.update { it.copy(peerId = userIdFull) }

        scope.launch {
            repeat(5) {
                val user = runCatching {
                    clientProvider().user.getById(roomId, UserId(userIdFull))
                        .filterNotNull()
                        .firstOrNull()
                }.getOrNull()

                if (user != null) {
                    _state.update {
                        it.copy(
                            peerName = user.name,
                            peerAvatarUrl = user.avatarUrl
                        )
                    }
                    return@launch
                }
                delay(250)
            }
            _state.update { it.copy(peerName = userIdFull) }
        }
    }
}
