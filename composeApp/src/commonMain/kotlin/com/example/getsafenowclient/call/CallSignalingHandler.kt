package com.example.getsafenowclient.call

import co.touchlab.kermit.Logger
import com.example.getsafenowclient.call.webrtc.WebRtcManager
import com.shepeliev.webrtckmp.IceCandidate
import com.shepeliev.webrtckmp.SessionDescription
import com.shepeliev.webrtckmp.SessionDescriptionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.client.room
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.events.m.call.CallEventContent
import net.folivo.trixnity.core.subscribeAsFlow
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Handles Matrix ↔ WebRTC signaling.
 * Globalized to listen for calls across ALL rooms.
 */
class CallSignalingHandler(
    private val clientProvider: () -> MatrixClient,
    private val webrtc: WebRtcManager,
    private val scope: CoroutineScope,
    private val myPartyId: String,
    private val dispatch: (CallEvent, RoomId) -> Unit,
) {
    private val client get() = clientProvider()
    private val myUserId get() = client.userId.full
    
    // The RoomId associated with the active call
    private var activeRoomId: RoomId? = null
    private var currentCallId: String? = null

    private val processedEventIds = mutableSetOf<String>()

    init {
        observeMatrixCallEvents()
        observeWebRtcEvents()
    }

    @OptIn(ExperimentalTime::class)
    private fun observeMatrixCallEvents() {
        scope.launch {
            // We need to wait for the client to be available before observing
            var matrixClient: MatrixClient? = null
            while(matrixClient == null) {
                try {
                    matrixClient = clientProvider()
                } catch (e: Exception) {
                    kotlinx.coroutines.delay(500)
                }
            }

            matrixClient.api.sync.subscribeAsFlow().collect { syncEvents ->
                val response = syncEvents.syncResponse
                val rooms = response.room?.join ?: return@collect

                for ((roomId, roomData) in rooms) {
                    val timeline = roomData.timeline?.events ?: continue

                    for (event in timeline) {
                        val eventId = event.id.full
                        if (processedEventIds.contains(eventId)) continue
                        processedEventIds.add(eventId)

                        val senderId = event.sender.full

                        // In Trixnity SyncResponse, event.content is the content itself.
                        val content = event.content

                        when (content) {
                            is CallEventContent.Invite -> {
                                if (senderId == myUserId) continue

                                // 🛑 GHOST CALL FIX: Check Event Age
                                val now = Clock.System.now().toEpochMilliseconds()
                                val eventAge = now - event.originTimestamp
                                if (eventAge > 60_000) { // 60 seconds tolerance
                                    Logger.w("CallSignaling → Ignoring Expired Invite (Age: ${eventAge}ms). EventID: $eventId")
                                    continue
                                }

                                if (currentCallId != null && currentCallId != content.callId) {
                                    // Glare Handling: Both parties called simultaneously.
                                    // Rule: Lexicographical comparison. If incoming CallID > my currentCallId, I yield.
                                    // I accept the incoming call (simulate receiving it) and ditch my own logic.
                                    if (content.callId > (currentCallId ?: "")) {
                                        Logger.i("Call Glare Detected! Yielding to incoming call ${content.callId} (mine was $currentCallId)")
                                        // Clear "my" call ID so we can blindly accept the new one
                                        currentCallId = null 
                                    } else {
                                        // I win. I assume the other side will do the same check and yield to me.
                                        Logger.i("Call Glare Detected! Ignoring incoming call ${content.callId} in favor of mine $currentCallId")
                                        continue
                                    }
                                }

                                currentCallId = content.callId
                                activeRoomId = roomId

                                val sdp = content.offer.sdp
                                val isVideo = sdp.contains("m=video") && !sdp.contains("m=video 0")

                                dispatch(
                                    CallEvent.IncomingInvite(
                                        callId = content.callId,
                                        opponentId = event.sender.full,
                                        offerSdp = content.offer.sdp,
                                        isVideo = isVideo
                                    ),
                                    roomId
                                )
                            }

                            is CallEventContent.Answer -> {
                                if (senderId == myUserId) {
                                    // Handle Answered Elsewhere vs. My Own Answer
                                    // If partyId matches, it's THIS device. Ignore.
                                    // If partyId differs, it's another login. Dispatch AnsweredElsewhere.
                                    if (currentCallId == content.callId && content.partyId != myPartyId) { 
                                         dispatch(CallEvent.AnsweredElsewhere(content.callId), roomId)
                                    }
                                    continue
                                }
                                if (currentCallId != content.callId) continue

                                dispatch(
                                    CallEvent.RemoteAnswered(
                                        callId = content.callId,
                                        answerSdp = content.answer.sdp
                                    ),
                                    roomId
                                )
                            }

                            is CallEventContent.Candidates -> {
                                if (senderId == myUserId) continue
                                if (currentCallId != content.callId) continue

                                content.candidates.forEach { c ->
                                    Logger.d("Signaling → Received ICE Candidate: ${c.candidate.substringBefore(' ')}...")
                                    dispatch(
                                        CallEvent.RemoteIceCandidate(
                                            sdp = c.candidate,
                                            sdpMid = c.sdpMid,
                                            sdpMLineIndex = c.sdpMLineIndex?.toInt()
                                        ),
                                        roomId
                                    )
                                }
                            }

                            is CallEventContent.Hangup -> {
                                if (currentCallId != content.callId) continue

                                val reason = when (content.reason) {
                                    CallEventContent.Hangup.Reason.USER_BUSY -> EndCallReason.Busy
                                    CallEventContent.Hangup.Reason.ICE_FAILED,
                                    CallEventContent.Hangup.Reason.ICE_TIMEOUT -> EndCallReason.IceFailed
                                    CallEventContent.Hangup.Reason.USER_HANGUP -> EndCallReason.RemoteHangup
                                    else -> EndCallReason.Error
                                }

                                dispatch(CallEvent.RemoteHangup(content.callId, reason), roomId)
                            }
                            else -> Unit
                        }
                    }
                }
            }
        }
    }

    private fun observeWebRtcEvents() {
        webrtc.onSessionDescription = { sdp ->
            scope.launch { sendMatrixSdp(sdp) }
        }
        webrtc.onIceCandidate = { ice ->
            scope.launch { sendMatrixIce(ice) }
        }
    }

    private suspend fun sendMatrixSdp(sdp: SessionDescription) {
        val rid = activeRoomId ?: return
        val cid = currentCallId ?: return

        when (sdp.type) {
            SessionDescriptionType.Offer -> client.room.sendMessage(rid) {
                content(
                    CallEventContent.Invite(
                        version = "1",
                        callId = cid,
                        partyId = myPartyId,
                        invitee = null,
                        lifetime = 60000,
                        offer = CallEventContent.Invite.Offer(sdp.sdp, CallEventContent.Invite.OfferType.OFFER),
                        sdpStreamMetadata = null
                    )
                )
            }
            SessionDescriptionType.Answer -> client.room.sendMessage(rid) {
                content(
                    CallEventContent.Answer(
                        version = "1",
                        callId = cid,
                        partyId = myPartyId,
                        answer = CallEventContent.Answer.Answer(sdp.sdp, CallEventContent.Answer.AnswerType.ANSWER),
                        sdpStreamMetadata = null
                    )
                )
            }
            else -> Unit
        }
    }

    private suspend fun sendMatrixIce(ice: IceCandidate) {
        val rid = activeRoomId ?: return
        val cid = currentCallId ?: return

        Logger.d("Signaling → Sending ICE Candidate: ${ice.candidate.substringBefore(' ')}...")
        client.room.sendMessage(rid) {
            content(
                CallEventContent.Candidates(
                    version = "1",
                    callId = cid,
                    partyId = myPartyId,
                    candidates = listOf(
                        CallEventContent.Candidates.Candidate(
                            candidate = ice.candidate,
                            sdpMLineIndex = ice.sdpMLineIndex.toLong(),
                            sdpMid = ice.sdpMid
                        )
                    )
                )
            )
        }
    }

    @OptIn(ExperimentalTime::class)
    fun startOutgoingCall(roomId: RoomId): String {
        val newId = "${roomId.full}-${myPartyId}-${Clock.System.now().toEpochMilliseconds()}"
        currentCallId = newId
        activeRoomId = roomId
        return newId
    }

    fun setActiveRoom(roomId: RoomId) {
        this.activeRoomId = roomId
    }

    fun clearCurrentCall() {
        currentCallId = null
        activeRoomId = null
    }

    suspend fun sendHangup() {
        val rid = activeRoomId ?: return
        val cid = currentCallId ?: return
        client.room.sendMessage(rid) {
            content(CallEventContent.Hangup("1", cid, myPartyId, null))
        }
    }

    suspend fun sendReject(roomId: RoomId, callId: String) {
        client.room.sendMessage(roomId) {
            content(CallEventContent.Hangup("1", callId, myPartyId, CallEventContent.Hangup.Reason.USER_HANGUP))
        }
    }

    suspend fun sendBusy(roomId: RoomId, callId: String) {
        client.room.sendMessage(roomId) {
            content(CallEventContent.Hangup("1", callId, myPartyId, CallEventContent.Hangup.Reason.USER_BUSY))
        }
    }
    
    fun notifyAnswerGenerated() {}
}
