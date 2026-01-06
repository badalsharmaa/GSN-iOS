package com.example.getsafenowclient.call.webrtc

import co.touchlab.kermit.Logger
import com.example.getsafenowclient.common.hardware.CallRingtoneController
import com.example.getsafenowclient.common.hardware.SpeakerController
import com.example.getsafenowclient.turn.TurnServerInfo
import com.shepeliev.webrtckmp.AudioTrack
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
import com.shepeliev.webrtckmp.VideoTrack
import com.shepeliev.webrtckmp.audioTracks
import com.shepeliev.webrtckmp.onConnectionStateChange
import com.shepeliev.webrtckmp.onIceCandidate
import com.shepeliev.webrtckmp.onIceConnectionStateChange
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
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

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
                Logger.d("WebRTC → LOCAL ICE Generated: ${ic.candidate} | mid: ${ic.sdpMid} | index: ${ic.sdpMLineIndex}")
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
                    IceConnectionState.Failed -> {
                        onIceFailed?.invoke()
                        startDisconnectWatchdog()
                    }
                    IceConnectionState.Disconnected -> startDisconnectWatchdog()
                    IceConnectionState.Connected, IceConnectionState.Completed -> cancelDisconnectWatchdog()
                    else -> Unit
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
                pc.setLocalDescription(offer)
                onSessionDescription?.invoke(offer)
            } catch (e: Exception) {
                Logger.e("startCall failed: $e")
                onCameraFailure?.invoke(e)
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

                pc.setRemoteDescription(SessionDescription(SessionDescriptionType.Offer, offerSdp))
                drainPendingCandidates()

                val answer = pc.createAnswer(OfferAnswerOptions(offerToReceiveAudio = true, offerToReceiveVideo = isVideo))
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
                if (remoteAnswerApplied) return@launch
                peerConnection?.setRemoteDescription(SessionDescription(SessionDescriptionType.Answer, sdp))
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

            val pc = peerConnection
            if (pc != null && pc.remoteDescription != null) {
                Logger.d("WebRTC → Applying Remote ICE immediately")
                pc.addIceCandidate(c)
            } else {
                Logger.d("WebRTC → Stashing Remote ICE (PeerConnection or RemoteDescription not ready)")
                pendingIceCandidates.add(c)
            }
        }
    }

    fun setSpeakerEnabled(enabled: Boolean) {
        speakerController?.setSpeakerEnabled(enabled)
    }

    private suspend fun drainPendingCandidates() {
        val pc = peerConnection ?: return
        Logger.d("WebRTC → Draining ${pendingIceCandidates.size} pending ICE candidates")
        val iter = pendingIceCandidates.iterator()
        while (iter.hasNext()) {
            val c = iter.next()
            try {
                pc.addIceCandidate(c)
                iter.remove()
            } catch (e: Exception) {
                Logger.e("Failed to apply stashed ICE candidate: $e")
            }
        }
    }

    fun setMicrophoneEnabled(enabled: Boolean) {
        _localStream.value?.audioTracks?.forEach { it.enabled = enabled }
    }

    fun setCameraEnabled(enabled: Boolean) {
        _localStream.value?.videoTracks?.forEach { it.enabled = enabled }
        if (!enabled) onLocalVideoStopped?.invoke()
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
