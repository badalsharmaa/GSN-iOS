package com.example.getsafenowclient.call

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

// Data classes for UI State
data class CallUiState(
    val callState: CallState = CallState.Idle,

    val roomId: String? = null,
    val peerId: String? = null,
    val peerName: String? = null,
    val peerAvatarUrl: String? = null,

    // Not whether the call is video right now → but what the user *intended*
    val isVideoCall: Boolean = false,

    // Actual video currently active/working
    val isVideoEnabled: Boolean = false,

    // Always independent of video state
    val isMicEnabled: Boolean = true,
    val isSpeakerEnabled: Boolean = false,

    val isMinimized: Boolean = false
)


sealed class CallState {

    /** No call happening */
    data object Idle : CallState()

    /** Outgoing call — we are ringing the remote party */
    data class OutgoingRinging(
        val callId: String,
        val opponentId: String
    ) : CallState()

    /** Incoming call — remote sent m.call.invite */
    data class IncomingRinging(
        val callId: String,
        val opponentId: String,
        val offerSdp: String
    ) : CallState()

    /** Local or remote answered — waiting for WebRTC ICE/sdp to connect */
    data class Connecting(
        val callId: String,
        val opponentId: String
    ) : CallState()

    /** WebRTC connected — media flowing */
    data class InCall(
        val callId: String,
        val opponentId: String,
        val startTimestamp: Long // Explicit timestamp required
    ) : CallState()

    /** Call ended for any reason */
    data class Ended(
        val callId: String?,
        val reason: EndCallReason
    ) : CallState()
}

enum class EndCallReason {
    LocalHangup,
    RemoteHangup,
    Rejected,
    Busy,
    IceFailed,
    ConnectionDropped,
    Error
}

