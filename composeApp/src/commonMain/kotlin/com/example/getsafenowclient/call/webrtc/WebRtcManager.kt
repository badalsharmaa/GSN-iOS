package com.example.getsafenowclient.call.webrtc

import co.touchlab.kermit.Logger
import com.example.getsafenowclient.common.hardware.CallRingtoneController
import com.example.getsafenowclient.common.hardware.SpeakerController
import com.example.getsafenowclient.turn.TurnServerInfo
import com.shepeliev.webrtckmp.ContinualGatheringPolicy
import com.shepeliev.webrtckmp.IceCandidate
import com.shepeliev.webrtckmp.IceConnectionState
import com.shepeliev.webrtckmp.IceServer
import com.shepeliev.webrtckmp.IceTransportPolicy
import com.shepeliev.webrtckmp.MediaDevices
import com.shepeliev.webrtckmp.MediaStream
import com.shepeliev.webrtckmp.MediaStreamTrackKind
import com.shepeliev.webrtckmp.OfferAnswerOptions
import com.shepeliev.webrtckmp.PeerConnection
import com.shepeliev.webrtckmp.PeerConnectionState
import com.shepeliev.webrtckmp.RtcConfiguration
import com.shepeliev.webrtckmp.SessionDescription
import com.shepeliev.webrtckmp.SessionDescriptionType
import com.shepeliev.webrtckmp.audioTracks
import com.shepeliev.webrtckmp.onConnectionStateChange
import com.shepeliev.webrtckmp.onIceCandidate
import com.shepeliev.webrtckmp.onIceConnectionStateChange
import com.shepeliev.webrtckmp.onNegotiationNeeded
import com.shepeliev.webrtckmp.onTrack
import com.shepeliev.webrtckmp.videoTracks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class WebRtcManager(
    private val scope: CoroutineScope,
    private val turnProvider: suspend () -> TurnServerInfo?
) {
    private val _localStream = MutableStateFlow<MediaStream?>(null)
    val localStream: StateFlow<MediaStream?> = _localStream.asStateFlow()

    var speakerController: SpeakerController? = null
    var ringtoneController: CallRingtoneController? = null

    private val _remoteStream = MutableStateFlow<MediaStream?>(null)
    val remoteStream: StateFlow<MediaStream?> = _remoteStream.asStateFlow()

    private val _connectionState = MutableStateFlow(PeerConnectionState.New)
    val connectionState: StateFlow<PeerConnectionState> = _connectionState.asStateFlow()

    private var peerConnection: PeerConnection? = null
    private val pendingIceCandidates = mutableListOf<IceCandidate>()
    private val trackMonitoringJobs = mutableMapOf<String, Job>()

    private var hasEverConnected = false
    private var disconnectWatchdogJob: Job? = null
    private var remoteAnswerApplied = false

    var onSessionDescription: ((SessionDescription) -> Unit)? = null
    var onIceCandidate: ((IceCandidate) -> Unit)? = null
    var onConnected: (() -> Unit)? = null
    var onDisconnected: (() -> Unit)? = null
    var onRemoteVideoStarted: (() -> Unit)? = null
    var onRemoteVideoStopped: (() -> Unit)? = null
    var onLocalVideoStopped: (() -> Unit)? = null
    var onCameraFailure: ((Throwable?) -> Unit)? = null
    var onIceFailed: (() -> Unit)? = null

    private fun startDisconnectWatchdog() {
        disconnectWatchdogJob?.cancel()
        disconnectWatchdogJob = scope.launch {
            delay(5000)
            if (hasEverConnected) {
                Logger.e("WebRTC Watchdog → REAL disconnect triggered")
                onDisconnected?.invoke()
            }
        }
    }

    private fun cancelDisconnectWatchdog() {
        disconnectWatchdogJob?.cancel()
        disconnectWatchdogJob = null
    }

    suspend fun setupLocalStream(video: Boolean, audio: Boolean) {
        try {
            Logger.d("WebRTC → setupLocalStream (video=$video, audio=$audio)")
            val stream = MediaDevices.getUserMedia(audio = audio, video = video)
            _localStream.value = stream

            peerConnection?.let { pc ->
                Logger.d("WebRTC → PeerConnection exists, attaching local tracks immediately")
                stream.tracks.forEach { track ->
                    pc.addTrack(track, stream)
                }
            }
        } catch (e: Exception) {
            Logger.e("setupLocalStream failed: $e")
            onCameraFailure?.invoke(e)
        }
    }

    private suspend fun ensurePeerConnection() {
        if (peerConnection != null) return

        Logger.d("WebRTC → Fetching TURN credentials...")
        val turn = try {
            turnProvider()
        } catch (e: Exception) {
            Logger.e("WebRTC → Failed to fetch TURN: $e")
            null
        }

        val iceServers = buildIceServers(turn)
        Logger.d("WebRTC → Creating PeerConnection with ${iceServers.size} ICE servers")

        val config = RtcConfiguration(
            iceServers = iceServers,
            iceTransportPolicy = IceTransportPolicy.All,
            iceCandidatePoolSize = 4,
            continualGatheringPolicy = ContinualGatheringPolicy.GatherContinually
        )
        val pc = PeerConnection(config)
        peerConnection = pc

        _localStream.value?.let { stream ->
            Logger.d("WebRTC → Adding ${stream.tracks.size} local tracks to PeerConnection")
            stream.tracks.forEach { track ->
                pc.addTrack(track, stream)
            }
        }

        attachPeerConnectionListeners(pc)
    }

    private fun buildIceServers(turn: TurnServerInfo?): List<IceServer> {
        val googleStun = listOf(
            IceServer(listOf("stun:stun.l.google.com:19302")),
            IceServer(listOf("stun:stun1.l.google.com:19302")),
            IceServer(listOf("stun:stun2.l.google.com:19302")),
            IceServer(listOf("stun:stun3.l.google.com:19302")),
            IceServer(listOf("stun:stun4.l.google.com:19302")),
        )

        if (turn == null || turn.uris.isEmpty()) {
            Logger.w("WebRTC → TURN not available, using STUN only")
            return googleStun
        }

        val turnServer = IceServer(
            urls = turn.uris,
            username = turn.username,
            password = turn.password
        )

        Logger.i("WebRTC → TURN enabled (uris=${turn.uris.joinToString()})")
        return listOf(turnServer) + googleStun
    }

    private fun attachPeerConnectionListeners(pc: PeerConnection) {
        pc.onIceCandidate
            .onEach { ic ->
                Logger.d("WebRTC → LOCAL ICE Generated: ${ic.candidate.substringBefore(' ')}... | mid: ${ic.sdpMid} | index: ${ic.sdpMLineIndex}")
                onIceCandidate?.invoke(ic)
            }
            .launchIn(scope)

        pc.onTrack
            .onEach { trackEvent ->
                val track = trackEvent.track ?: return@onEach
                val stream = trackEvent.streams.firstOrNull()
                Logger.d("WebRTC → REMOTE TRACK Received: ${track.kind} | ID: ${track.id}")

                if (stream != null) _remoteStream.value = stream

                if (track.kind == MediaStreamTrackKind.Video) {
                    val id = track.id
                    trackMonitoringJobs[id]?.cancel()

                    trackMonitoringJobs[id] = scope.launch {
                        var last = track.enabled
                        if (last) onRemoteVideoStarted?.invoke()
                        else onRemoteVideoStopped?.invoke()

                        while (isActive) {
                            delay(400)
                            val now = track.enabled
                            if (now != last) {
                                last = now
                                if (now) onRemoteVideoStarted?.invoke()
                                else onRemoteVideoStopped?.invoke()
                            }
                        }
                    }
                }
            }
            .launchIn(scope)

        pc.onConnectionStateChange
            .onEach { state ->
                Logger.d("WebRTC Connection State = $state")
                _connectionState.value = state

                when (state) {
                    PeerConnectionState.Connected -> {
                        Logger.i("WebRTC STABLE CONNECTED")
                        hasEverConnected = true
                        cancelDisconnectWatchdog()
                        onConnected?.invoke()
                    }
                    PeerConnectionState.Disconnected -> startDisconnectWatchdog()
                    PeerConnectionState.Failed -> {
                        Logger.e("WebRTC Connection FAILED")
                        onIceFailed?.invoke()
                        startDisconnectWatchdog()
                    }
                    PeerConnectionState.Closed -> {
                        cancelDisconnectWatchdog()
                        onDisconnected?.invoke()
                    }
                    else -> Unit
                }
            }
            .launchIn(scope)

        pc.onIceConnectionStateChange
            .onEach { ice ->
                Logger.d("WebRTC ICE State = $ice")
                when (ice) {
                    IceConnectionState.Connected, IceConnectionState.Completed -> {
                        Logger.i("WebRTC ICE Connected! Triggering connection success fallback.")
                        // Fallback: If ICE connects but PeerConnection stays in 'Connecting', treat as connected.
                        // This happens on some iOS versions/network topologies.
                        hasEverConnected = true
                        cancelDisconnectWatchdog()
                        onConnected?.invoke()
                    }
                    IceConnectionState.Failed -> {
                        Logger.e("WebRTC ICE FAILED")
                        onIceFailed?.invoke()
                        startDisconnectWatchdog()
                    }
                    IceConnectionState.Disconnected -> startDisconnectWatchdog()
                    else -> Unit
                }
            }
            .launchIn(scope)

        pc.onNegotiationNeeded
            .onEach {
                // RENEGOTIATION: Triggered when tracks are added/removed
                // We must create a new offer to update the remote peer.
                Logger.i("WebRTC → Negotiation Needed triggered")
                // Only the offerer (or if we are stable/connected) should kick this off typically,
                // but since we are changing tracks locally, we initiate.
                val connectionState = _connectionState.value
                if (connectionState == PeerConnectionState.Connected || 
                    connectionState == PeerConnectionState.Connecting ||
                    connectionState == PeerConnectionState.New) { // Catch 'New' if tracks added before start
                    
                    try {
                        Logger.d("WebRTC → Creating Renegotiation Offer")
                        // Ensure we offer to receive both A/V if we handle them
                        val options = OfferAnswerOptions(offerToReceiveAudio = true, offerToReceiveVideo = true)
                        val offer = pc.createOffer(options)
                        pc.setLocalDescription(offer)
                        onSessionDescription?.invoke(offer)
                    } catch (e: Exception) {
                        Logger.e("Renegotiation failed: $e")
                    }
                }
            }
            .launchIn(scope)
    }

    fun startCall(isVideo: Boolean) {
        Logger.d("WebRTC → startCall (isVideo=$isVideo)")
        remoteAnswerApplied = false
        cancelDisconnectWatchdog()
        hasEverConnected = false

        scope.launch {
            try {
                ensurePeerConnection()
                val pc = peerConnection ?: return@launch

                val offer = pc.createOffer(OfferAnswerOptions(offerToReceiveAudio = true, offerToReceiveVideo = isVideo))
                Logger.d("WebRTC → Created Offer (hasVideo=$isVideo)")
                pc.setLocalDescription(offer)
                onSessionDescription?.invoke(offer)
            } catch (e: Exception) {
                Logger.e("startCall failed: $e")
                onCameraFailure?.invoke(e)
            }
        }
    }

    fun restartIce() {
        Logger.i("WebRTC → restartIce() initiated")
        scope.launch {
            try {
                val pc = peerConnection ?: return@launch
                // Create offer with iceRestart = true
                val options = OfferAnswerOptions(
                    offerToReceiveAudio = true,
                    offerToReceiveVideo = true,
                    iceRestart = true
                )
                val offer = pc.createOffer(options)
                pc.setLocalDescription(offer)
                onSessionDescription?.invoke(offer)
            } catch (e: Exception) {
                Logger.e("restartIce failed: $e")
                onIceFailed?.invoke()
            }
        }
    }

    fun acceptCall(offerSdp: String, isVideo: Boolean) {
        Logger.d("WebRTC → acceptCall (isVideo=$isVideo)")
        remoteAnswerApplied = false
        cancelDisconnectWatchdog()
        hasEverConnected = false

        scope.launch {
            try {
                ensurePeerConnection()
                val pc = peerConnection ?: return@launch

                Logger.d("WebRTC → Setting Remote Offer...")
                pc.setRemoteDescription(SessionDescription(SessionDescriptionType.Offer, offerSdp))
                drainPendingCandidates()

                val answer = pc.createAnswer(OfferAnswerOptions(offerToReceiveAudio = true, offerToReceiveVideo = isVideo))
                Logger.d("WebRTC → Created Answer")
                pc.setLocalDescription(answer)
                onSessionDescription?.invoke(answer)
            } catch (e: Exception) {
                Logger.e("acceptCall failed: $e")
                onCameraFailure?.invoke(e)
            }
        }
    }

    fun handleRemoteAnswer(sdp: String) {
        Logger.d("WebRTC → handleRemoteAnswer received")
        scope.launch {
            try {
                if (remoteAnswerApplied) {
                    Logger.w("WebRTC → Ignoring duplicate Answer")
                    return@launch
                }
                peerConnection?.setRemoteDescription(SessionDescription(SessionDescriptionType.Answer, sdp))
                Logger.d("WebRTC → Remote Answer Applied")
                remoteAnswerApplied = true
                drainPendingCandidates()
            } catch (e: Exception) {
                Logger.e("handleRemoteAnswer failed: $e")
            }
        }
    }

    fun handleRemoteCandidate(sdp: String, mid: String?, index: Int?) {
        scope.launch {
            val safeMid = mid ?: "0"
            val c = IceCandidate(sdpMid = safeMid, sdpMLineIndex = index ?: 0, candidate = sdp)
            Logger.d("WebRTC → Received Remote Candidate: ${sdp.substringBefore(' ')}... queueing.")

            // Robustness Fix:
            // ALWAYS add to the queue first. Then try to drain immediately.
            // This ensures order is preserved even if pc.remoteDescription is momentarily null
            // but becomes available in the very next event loop tick.
            pendingIceCandidates.add(c)
            drainPendingCandidates()
        }
    }

    fun setSpeakerEnabled(enabled: Boolean) {
        speakerController?.setSpeakerEnabled(enabled)
    }

    private suspend fun drainPendingCandidates() {
        val pc = peerConnection ?: return
        
        // Critical Check: Can we actually add candidates yet?
        // WebRTC requires setRemoteDescription to be called first.
        if (pc.remoteDescription == null) {
            Logger.d("WebRTC → Cannot drain ICE yet: RemoteDescription is null. Queue size: ${pendingIceCandidates.size}")
            return
        }

        if (pendingIceCandidates.isNotEmpty()) {
            Logger.d("WebRTC → Draining ${pendingIceCandidates.size} pending ICE candidates")
            val iter = pendingIceCandidates.iterator()
            while (iter.hasNext()) {
                val c = iter.next()
                try {
                    pc.addIceCandidate(c)
                    iter.remove()
                    Logger.v("WebRTC → Added ICE Candidate: ${c.candidate.substringBefore(' ')}...")
                } catch (e: Exception) {
                    Logger.e("Failed to apply stashed ICE candidate: $e")
                }
            }
        }
    }

    fun setMicrophoneEnabled(enabled: Boolean) {
        _localStream.value?.audioTracks?.forEach { it.enabled = enabled }
    }

    fun setCameraEnabled(enabled: Boolean) {
        // Robust Camera Toggle: Release hardware on disable, Re-acquire on enable
        scope.launch {
            try {
                if (enabled) {
                    Logger.d("WebRTC → Enabling Camera")
                    // 1. Check if we already have a live track (unlikely if we stopped it, but good safety)
                    val existingTrack = _localStream.value?.videoTracks?.firstOrNull()
                    if (existingTrack != null && existingTrack.state.value is com.shepeliev.webrtckmp.MediaStreamTrackState.Live) {
                        Logger.d("WebRTC → Existing track is live, just enabling")
                        existingTrack.enabled = true
                        return@launch
                    }

                    // 2. Acquire new Video Track (Video Only)
                    Logger.d("WebRTC → Requesting new UserMedia (Video)")
                    val videoStream = MediaDevices.getUserMedia(audio = false, video = true)
                    val newVideoTrack = videoStream.videoTracks.firstOrNull()

                    if (newVideoTrack != null) {
                        val currentStream = _localStream.value
                        if (currentStream != null) {
                             // Add new track to the existing local stream (for UI preview)
                             // Remove old dead tracks first to keep it clean
                             currentStream.videoTracks.forEach { 
                                 currentStream.removeTrack(it) 
                             }
                             currentStream.addTrack(newVideoTrack)
                        } else {
                            // Should theoretically not happen in a call if audio is there, but fallback:
                            _localStream.value = videoStream
                        }

                        // 3. Update PeerConnection (Stop old sender, Add new track)
                        val pc = peerConnection
                        if (pc != null) {
                            Logger.d("WebRTC → Replacing Video Track in PeerConnection")
                            val senders = pc.getSenders()
                            val videoSender = senders.find { it.track?.kind == MediaStreamTrackKind.Video }
                            
                            if (videoSender != null) {
                                // Since replaceTrack is missing/unresolved, we use remove + add fallback
                                // This might trigger renegotiation
                                Logger.d("WebRTC → Removing old video track sender")
                                pc.removeTrack(videoSender)
                            }
                            
                            Logger.d("WebRTC → Adding new video track")
                            pc.addTrack(newVideoTrack, currentStream ?: videoStream)
                            // Manual renegotiation removed in favor of onNegotiationNeeded listener
                        }
                    }
                } else {
                    Logger.d("WebRTC → Disabling Camera (Stopping tracks)")
                    // 1. Stop all video tracks to release hardware
                     _localStream.value?.videoTracks?.forEach { 
                         it.enabled = false
                         it.stop() 
                     }
                     
                    // 2. Remove track from PeerConnection to trigger renegotiation (and stop transmission)
                    // This ensures the remote side receives a new SDP (m=video 0) and doesn't see a frozen frame.
                    val pc = peerConnection
                    if (pc != null) {
                        val senders = pc.getSenders()
                        val videoSender = senders.find { it.track?.kind == MediaStreamTrackKind.Video }
                        if (videoSender != null) {
                             Logger.d("WebRTC → Removing Video Sender to enforce privacy")
                             pc.removeTrack(videoSender)
                        }
                    }
                }
            } catch (e: Exception) {
                Logger.e("setCameraEnabled failed: $e")
                onCameraFailure?.invoke(e)
            }
        }
    }

    fun switchCamera() {
        scope.launch {
            try {
                _localStream.value?.videoTracks?.firstOrNull()?.switchCamera()
            } catch (e: Exception) {
                onCameraFailure?.invoke(e)
            }
        }
    }

    fun close() {
        Logger.e("WebRTC → close() called, releasing resources")
        remoteAnswerApplied = false
        cancelDisconnectWatchdog()
        trackMonitoringJobs.values.forEach { it.cancel() }
        trackMonitoringJobs.clear()
        _localStream.value?.tracks?.forEach { it.stop() }
        _remoteStream.value?.tracks?.forEach { it.stop() }
        peerConnection?.close()
        peerConnection = null
        pendingIceCandidates.clear()
        _localStream.value = null
        _remoteStream.value = null
        _connectionState.value = PeerConnectionState.Closed
    }
}
