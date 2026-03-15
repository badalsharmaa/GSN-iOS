package com.example.getsafenowclient.call

import com.example.getsafenowclient.call.webrtc.WebRtcManager
import com.example.getsafenowclient.call.repository.CallStateRepository
import com.example.getsafenowclient.call.repository.IncomingCallData
import com.example.getsafenowclient.permissions.CameraPermission
import com.example.getsafenowclient.permissions.MicrophonePermission
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
    private val clientProvider: () -> MatrixClient,
    private val backgroundManager: CallBackgroundManager,
    private val cameraPermission: CameraPermission,
    private val microphonePermission: MicrophonePermission,
    private val callStateRepository: CallStateRepository  // ✅ Added for persistent storage
) {

    private val _state = MutableStateFlow(CallUiState())
    val state: StateFlow<CallUiState> = _state.asStateFlow()

    init {
        observeWebRtcCallbacks()
        
        backgroundManager.onAnswer = {
            if (_state.value.callState is CallState.IncomingRinging) {
                dispatch(CallEvent.AcceptCall)
            } else {
                // Race condition: User answered on native UI, but Sync hasn't delivered invite yet.
                _state.update { it.copy(pendingAnswer = true) }
            }
        }
        backgroundManager.onHangup = {
             dispatch(CallEvent.Hangup) // Or RejectCall depending on state
        }
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
        // PERMISSION CHECK (Caller Side)
        if (!microphonePermission.hasPermission()) {
            val granted = microphonePermission.requestPermission()
            if (!granted) return // TODO: Handle denial gracefully (toast?)
        }
        if (isVideo && !cameraPermission.hasPermission()) {
            val granted = cameraPermission.requestPermission()
            if (!granted) return
        }
        
        // Start background service & audio focus
        backgroundManager.startBackgroundExecution(isIncoming = false)
        backgroundManager.requestAudioFocus()

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
        // Timeout logic
        scope.launch {
            delay(60_000) // 60s timeout
            val current = _state.value.callState
            if (current is CallState.OutgoingRinging && current.callId == newCallId) {
                endCall(EndCallReason.Timeout)
            }
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
            CallEvent.WebRtcDisconnected -> handleDisconnect(current)
            CallEvent.ConnectionRestored -> onWebRtcConnected(current)
            
            is CallEvent.AnsweredElsewhere -> endCall(EndCallReason.AnsweredElsewhere)

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

    private suspend fun acceptIncomingCall(current: CallUiState) {
        val st = current.callState
        if (st !is CallState.IncomingRinging) return
        val rid = current.roomId?.let { RoomId(it) } ?: return

        // ✅ Validate SDP offer before accepting
        if (st.offerSdp.isBlank()) {
            co.touchlab.kermit.Logger.e { "Cannot accept call: SDP offer is empty" }
            endCall(EndCallReason.Error)
            return
        }

        // PERMISSION CHECK (Receiver Side)
        // Check permissions before answering. If missing, request.
        val granted = microphonePermission.requestPermission()
        if (current.isVideoCall) {
            val cameraGranted = cameraPermission.requestPermission()
            if (!cameraGranted) return
        } else {
            if (!granted) return
        }

        webrtc.ringtoneController?.stopAllTones()

        // Wait for hardware to init (prevent empty audio/video)
        localStreamSetupJob?.join()

        // ✅ Log SDP for debugging
        co.touchlab.kermit.Logger.d { 
            "Accepting call with SDP offer (first 100 chars): ${st.offerSdp.take(100)}..." 
        }

        webrtc.acceptCall(st.offerSdp, current.isVideoCall)
        signaling.notifyAnswerGenerated()

        // ✅ Clear persisted state after accepting
        scope.launch {
            try {
                callStateRepository.clearCall()
                co.touchlab.kermit.Logger.d { "✅ Cleared persisted call state after accept" }
            } catch (e: Exception) {
                co.touchlab.kermit.Logger.e(e) { "Failed to clear call state" }
            }
        }

        _state.update {
            it.copy(callState = CallState.Connecting(st.callId, st.opponentId))
        }
    }

    private suspend fun rejectIncomingCall(current: CallUiState) {
        val st = current.callState
        if (st !is CallState.IncomingRinging) return
        val rid = current.roomId?.let { RoomId(it) } ?: return

        webrtc.ringtoneController?.stopAllTones()

        // ✅ Clear persisted state before rejecting
        scope.launch {
            try {
                callStateRepository.clearCall()
                co.touchlab.kermit.Logger.d { "✅ Cleared persisted call state after reject" }
            } catch (e: Exception) {
                co.touchlab.kermit.Logger.e(e) { "Failed to clear call state" }
            }
        }

        signaling.sendReject(rid, st.callId)
        endCall(EndCallReason.Rejected)
    }

    // --------------------------------------------------------
    // SIGNALING HANDLERS
    // ---------------------------------------------------------
    private fun onIncomingInvite(e: CallEvent.IncomingInvite) {
    }
    
    private var localStreamSetupJob: kotlinx.coroutines.Job? = null

    // Internal version of onIncomingInvite that accepts RoomId
    fun handleIncomingInvite(e: CallEvent.IncomingInvite, roomId: RoomId) {
        // RENEGOTIATION CHECK:
        // If we are already in a call with the SAME Call ID, this is a renegotiation (e.g. camera toggle).
        // We should silently accept it instead of rejecting it as busy.
        val currentCallId = when (val st = _state.value.callState) {
            is CallState.InCall -> st.callId
            is CallState.Connecting -> st.callId
            else -> null
        }

        if (currentCallId != null && currentCallId == e.callId) {
             co.touchlab.kermit.Logger.i("CallScreenModel → Received Renegotiation Invite (Same Call ID). Accepting silently.")
             scope.launch {
                 webrtc.acceptCall(e.offerSdp, e.isVideo)
             }
             return
        }

        if (_state.value.callState !is CallState.Idle) {
            scope.launch { signaling.sendBusy(roomId, e.callId) }
            return
        }
        
        // ✅ Persist call state immediately for app restart recovery
        scope.launch {
            try {
                val callData = IncomingCallData(
                    roomId = roomId.full,
                    callId = e.callId,
                    offerSdp = e.offerSdp,
                    isVideo = e.isVideo,
                    opponentId = e.opponentId
                )
                callStateRepository.saveIncomingCall(callData)
                co.touchlab.kermit.Logger.d { 
                    "✅ Persisted incoming call state: callId=${e.callId} roomId=${roomId.full}" 
                }
            } catch (ex: Exception) {
                co.touchlab.kermit.Logger.e(ex) { "Failed to persist call state" }
            }
        }
        
        webrtc.ringtoneController?.startRingtone()
        updatePeerInfo(roomId, e.opponentId)

        // Start background service & audio focus
        backgroundManager.startBackgroundExecution(isIncoming = true, callerName = e.opponentId)
        backgroundManager.requestAudioFocus()

        localStreamSetupJob = scope.launch {
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

        // Auto-accept if user already answered via CallKit/Notification
        if (_state.value.pendingAnswer) {
            scope.launch {
                // Clear flag
                _state.update { it.copy(pendingAnswer = false) }
                // Accept
                acceptIncomingCall(_state.value) 
            }
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
        if (st is CallState.Connecting || st is CallState.Reconnecting) {
            val callId = if (st is CallState.Connecting) st.callId else (st as CallState.Reconnecting).callId
            val opponentId = if (st is CallState.Connecting) st.opponentId else (st as CallState.Reconnecting).opponentId

            _state.update {
                it.copy(
                    callState = CallState.InCall(
                        callId = callId,
                        opponentId = opponentId,
                        startTimestamp = Clock.System.now().toEpochMilliseconds()
                    )
                )
            }
            // 🎤 FIX FOR iOS INITIAL MUTE:
            webrtc.setMicrophoneEnabled(current.isMicEnabled)
        }
    }
    
    private fun handleDisconnect(current: CallUiState) {
        if (current.callState is CallState.InCall || current.callState is CallState.Connecting) {
            // Attempt Reconnection
            val (callId, opponentId) = when (val st = current.callState) {
                is CallState.InCall -> st.callId to st.opponentId
                is CallState.Connecting -> st.callId to st.opponentId
                else -> return
            }

            _state.update {
                it.copy(callState = CallState.Reconnecting(callId, opponentId))
            }
            webrtc.restartIce()
        } else {
            endCall(EndCallReason.ConnectionDropped)
        }
    }

    private fun disableVideoFallback() {
        _state.update { it.copy(isVideoEnabled = false) }
        webrtc.setCameraEnabled(false)
    }

    private fun endCall(reason: EndCallReason) {
        co.touchlab.kermit.Logger.i("CallScreenModel → endCall (reason=$reason)")
        
        // ✅ Clear persisted call state on any end
        scope.launch {
            try {
                callStateRepository.clearCall()
                co.touchlab.kermit.Logger.d { "✅ Cleared persisted call state on endCall" }
            } catch (e: Exception) {
                co.touchlab.kermit.Logger.e(e) { "Failed to clear call state" }
            }
        }
        
        webrtc.ringtoneController?.stopAllTones()
        signaling.clearCurrentCall()
        webrtc.close()
        
        backgroundManager.releaseAudioFocus()
        backgroundManager.stopBackgroundExecution()

        _state.update {
            val callId = when (val st = it.callState) {
                is CallState.InCall -> st.callId
                is CallState.Connecting -> st.callId
                is CallState.OutgoingRinging -> st.callId
                is CallState.IncomingRinging -> st.callId
                is CallState.Reconnecting -> st.callId
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
    
    /**
     * Handle remote hangup event from Matrix.
     * Called when the other party ends the call.
     */
    fun onRemoteHangup(callId: String) {
        co.touchlab.kermit.Logger.i("CallScreenModel → Remote hangup received for call: $callId")
        
        // ✅ Clear persisted state
        scope.launch {
            try {
                callStateRepository.clearCall()
                co.touchlab.kermit.Logger.d { "✅ Cleared persisted call state after remote hangup" }
            } catch (e: Exception) {
                co.touchlab.kermit.Logger.e(e) { "Failed to clear call state" }
            }
        }
        
        // ✅ Dismiss notification if exists
        backgroundManager.stopBackgroundExecution()
        
        // End call with remote hangup reason
        endCall(EndCallReason.RemoteHangup)
    }

    private fun observeWebRtcCallbacks() {
        webrtc.onConnected = { dispatch(CallEvent.WebRtcConnected) }
        webrtc.onDisconnected = { dispatch(CallEvent.WebRtcDisconnected) }
        webrtc.onCameraFailure = { dispatch(CallEvent.LocalCameraFailed(it)) }
        webrtc.onRemoteVideoStopped = { dispatch(CallEvent.RemoteVideoStopped) }
        webrtc.onLocalVideoStopped = { dispatch(CallEvent.LocalVideoStopped) }
    }

    /**
     * Resume a call from notification/intent (handles both incoming and outgoing)
     */
    fun resumeCall(
        callId: String,
        callerName: String?,
        isVideo: Boolean,
        isIncoming: Boolean
    ) {
        val current = _state.value
        
        // Optimistic Restoration: If we are fresh (Idle) but have a call intent, 
        // show the appropriate UI immediately.
        if (current.callState is CallState.Idle) {
            if (isIncoming) {
                // Incoming call - show ringing UI
                _state.update {
                    it.copy(
                        roomId = "pending_sync", // Placeholder until sync provides real roomId
                        peerId = callerName ?: "Unknown",
                        peerName = callerName,
                        callState = CallState.IncomingRinging(
                            callId = callId,
                            opponentId = callerName ?: "Unknown",
                            offerSdp = "" // Placeholder - CallSignalingHandler will provide real SDP
                        ),
                        isVideoCall = isVideo,
                        isVideoEnabled = isVideo,
                        isMinimized = false
                    )
                }
            } else {
                // ✅ Outgoing call - show outgoing ringing UI
                _state.update {
                    it.copy(
                        roomId = "pending_sync",
                        peerId = callerName ?: "Unknown",
                        peerName = callerName,
                        callState = CallState.OutgoingRinging(
                            callId = callId,
                            opponentId = callerName ?: "Unknown"
                        ),
                        isVideoCall = isVideo,
                        isVideoEnabled = isVideo,
                        isMinimized = false
                    )
                }
            }
            // TRIGGER SIGNALING/SYNC: 
            // SessionManager.startSync() should already be running.
            // CallSignalingHandler will eventually overwrite this state with the REAL invite.
            return
        }

        // If already in a call state, just un-minimize
        if (current.callState is CallState.IncomingRinging ||
            current.callState is CallState.OutgoingRinging ||
            current.callState is CallState.Connecting ||
            current.callState is CallState.InCall ||
            current.callState is CallState.Reconnecting
        ) {
            _state.update { it.copy(isMinimized = false) }
        }
    }
    
    // ✅ Backward compatibility alias
    @Deprecated("Use resumeCall instead", ReplaceWith("resumeCall(callId, callerName, isVideo, isIncoming)"))
    fun resumeIncomingCall(
        callId: String,
        callerName: String?,
        isVideo: Boolean,
        isIncoming: Boolean
    ) = resumeCall(callId, callerName, isVideo, isIncoming)
    
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
