package com.example.getsafenowclient.call

// All call actions (UI + internal)
sealed class CallEvent {

    // -------------------------
    //❗ USER ACTION EVENTS
    // -------------------------

    data object ToggleMic : CallEvent()
    data object ToggleCamera : CallEvent()
    data object ToggleSpeaker : CallEvent()
    data object SwitchCamera : CallEvent()

    data object Minimize : CallEvent()
    data object Restore : CallEvent()

    data object Hangup : CallEvent()
    data object AcceptCall : CallEvent()
    data object RejectCall : CallEvent()


    // -------------------------
    //  INTERNAL SIGNALLING EVENTS
    // -------------------------

    // Received m.call.invite
    data class IncomingInvite(
        val callId: String,
        val opponentId: String,
        val offerSdp: String,
        val isVideo: Boolean
    ) : CallEvent()

    // Received m.call.answer
    data class RemoteAnswered(
        val callId: String,
        val answerSdp: String
    ) : CallEvent()

    // Received m.call.candidates
    data class RemoteIceCandidate(
        val sdp: String,
        val sdpMid: String?,
        val sdpMLineIndex: Int?
    ) : CallEvent()

    // Received m.call.hangup
    data class RemoteHangup(
        val callId: String,
        val reason: EndCallReason
    ) : CallEvent()

    // Answered on another device
    data class AnsweredElsewhere(val callId: String) : CallEvent()


    // -------------------------
    //  INTERNAL WEBRTC EVENTS
    // -------------------------

    data object WebRtcConnected : CallEvent()
    data object WebRtcDisconnected : CallEvent()
    data object ConnectionRestored : CallEvent()

    data class LocalCameraFailed(val error: Throwable?) : CallEvent()
    data object RemoteVideoStopped : CallEvent()
    data object LocalVideoStopped : CallEvent()

    // ICE failures
    data object IceFailed : CallEvent()
    data object IceTimeout : CallEvent()
}
