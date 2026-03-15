package com.example.getsafenowclient.utils

import com.example.getsafenowclient.call.CallBubbleType
import com.example.getsafenowclient.utils.CallUtils.isVideoOffer
import net.folivo.trixnity.client.store.TimelineEvent
import net.folivo.trixnity.core.model.UserId
import net.folivo.trixnity.core.model.events.m.call.CallEventContent
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Unified call state resolver - Single source of truth for call states.
 * Used by both Home screen (preview text) and Room screen (bubble rendering).
 */
object CallStateResolver {
    
    /**
     * Timeout in milliseconds after which a call without answer/hangup is considered stale.
     */
    const val CALL_TIMEOUT_MS = 60_000L // 60 seconds
    
    /**
     * Extract call ID from any call event.
     */
    fun getCallId(content: Any?): String? {
        return when (content) {
            is CallEventContent.Invite -> content.callId
            is CallEventContent.Answer -> content.callId
            is CallEventContent.Hangup -> content.callId
            is CallEventContent.Candidates -> content.callId
            else -> null
        }
    }
    
    /**
     * Complete call state information.
     */
    data class CallState(
        val type: CallStateType,
        val isVideo: Boolean,
        val isOutgoing: Boolean,
        val durationMs: Long?,
        val previewText: String,
        val bubbleType: CallBubbleType
    )
    
    /**
     * High-level call state types.
     */
    enum class CallStateType {
        ANSWERED,      // Call was answered and ended normally
        MISSED,        // Incoming call not answered
        CANCELLED,     // Outgoing call cancelled before answer
        BUSY,          // User was busy
        FAILED,        // Technical failure (ICE, media, etc.)
        RINGING,       // Currently ringing (active)
        ONGOING        // Currently in progress (answered but not ended)
    }
    
    /**
     * Analyzes a call sequence and returns unified state.
     * 
     * @param events List of timeline events (should include all events for the call)
     * @param callId The call ID to analyze
     * @param currentUserId The current user's ID
     * @return Complete call state information
     */
    @OptIn(ExperimentalTime::class)
    fun resolveCallState(
        events: List<TimelineEvent>,
        callId: String,
        currentUserId: UserId
    ): CallState {
        // Filter events for this specific call
        val callEvents = events.filter { event ->
            val content = event.content?.getOrNull()
            when (content) {
                is CallEventContent.Invite -> content.callId == callId
                is CallEventContent.Answer -> content.callId == callId
                is CallEventContent.Hangup -> content.callId == callId
                is CallEventContent.Candidates -> content.callId == callId
                else -> false
            }
        }.sortedBy { it.event.originTimestamp }
        
        // Extract key events
        val inviteEvent = callEvents.firstOrNull { 
            it.content?.getOrNull() is CallEventContent.Invite 
        }
        val answerEvent = callEvents.firstOrNull { 
            it.content?.getOrNull() is CallEventContent.Answer 
        }
        val hangupEvent = callEvents.lastOrNull { 
            it.content?.getOrNull() is CallEventContent.Hangup 
        }
        
        // If no invite, return unknown state
        if (inviteEvent == null) {
            return CallState(
                type = CallStateType.FAILED,
                isVideo = false,
                isOutgoing = false,
                durationMs = null,
                previewText = "[unknown call]",
                bubbleType = CallBubbleType.FAILED
            )
        }
        
        val inviteContent = inviteEvent.content?.getOrNull() as CallEventContent.Invite
        val isOutgoing = inviteEvent.event.sender == currentUserId
        val isVideo = isVideoOffer(inviteContent.offer.sdp)
        
        return when {
            // Call was answered and ended
            answerEvent != null && hangupEvent != null -> {
                val durationMs = (hangupEvent.event.originTimestamp - answerEvent.event.originTimestamp)
                    .takeIf { it > 0 }
                
                CallState(
                    type = CallStateType.ANSWERED,
                    isVideo = isVideo,
                    isOutgoing = isOutgoing,
                    durationMs = durationMs,
                    previewText = if (isVideo) "Video call" else "Voice call",
                    bubbleType = if (isOutgoing) CallBubbleType.OUTGOING_ENDED else CallBubbleType.INCOMING_ENDED
                )
            }
            
            // Call was answered but still ongoing (rare in preview)
            answerEvent != null && hangupEvent == null -> {
                CallState(
                    type = CallStateType.ONGOING,
                    isVideo = isVideo,
                    isOutgoing = isOutgoing,
                    durationMs = null,
                    previewText = if (isVideo) "Video call" else "Voice call",
                    bubbleType = if (isOutgoing) CallBubbleType.OUTGOING_ENDED else CallBubbleType.INCOMING_ENDED
                )
            }
            
            // Call ended without being answered (missed/cancelled)
            hangupEvent != null && answerEvent == null -> {
                val hangupContent = hangupEvent.content?.getOrNull() as? CallEventContent.Hangup
                val reason = hangupContent?.reason
                
                when (reason) {
                    CallEventContent.Hangup.Reason.USER_BUSY -> CallState(
                        type = CallStateType.BUSY,
                        isVideo = isVideo,
                        isOutgoing = isOutgoing,
                        durationMs = null,
                        previewText = if (isOutgoing) "Busy" else "Missed call",
                        bubbleType = if (isOutgoing) CallBubbleType.OUTGOING_MISSED else CallBubbleType.INCOMING_MISSED
                    )
                    
                    CallEventContent.Hangup.Reason.USER_HANGUP -> CallState(
                        type = if (isOutgoing) CallStateType.CANCELLED else CallStateType.MISSED,
                        isVideo = isVideo,
                        isOutgoing = isOutgoing,
                        durationMs = null,
                        previewText = if (isOutgoing) "Cancelled call" else "Missed call",
                        bubbleType = if (isOutgoing) CallBubbleType.OUTGOING_CANCELLED else CallBubbleType.INCOMING_MISSED
                    )
                    
                    CallEventContent.Hangup.Reason.ICE_FAILED,
                    CallEventContent.Hangup.Reason.ICE_TIMEOUT,
                    CallEventContent.Hangup.Reason.USER_MEDIA_FAILED -> CallState(
                        type = CallStateType.FAILED,
                        isVideo = isVideo,
                        isOutgoing = isOutgoing,
                        durationMs = null,
                        previewText = "Call failed",
                        bubbleType = CallBubbleType.FAILED
                    )
                    
                    else -> CallState(
                        type = if (isOutgoing) CallStateType.CANCELLED else CallStateType.MISSED,
                        isVideo = isVideo,
                        isOutgoing = isOutgoing,
                        durationMs = null,
                        previewText = if (isOutgoing) "Cancelled call" else "Missed call",
                        bubbleType = if (isOutgoing) CallBubbleType.OUTGOING_CANCELLED else CallBubbleType.INCOMING_MISSED
                    )
                }
            }
            
            // Call is still ringing (only invite, no answer or hangup)
            else -> {
                // Check if the call is stale (older than timeout threshold)
                val callAge = Clock.System.now().toEpochMilliseconds() - inviteEvent.event.originTimestamp
                val isStale = callAge > CALL_TIMEOUT_MS

                if (isStale) {
                    // Stale call treated as missed
                    CallState(
                        type = CallStateType.MISSED,
                        isVideo = isVideo,
                        isOutgoing = isOutgoing,
                        durationMs = null,
                        previewText = if (isOutgoing) "Unavailable" else "Missed call",
                        bubbleType = if (isOutgoing) CallBubbleType.OUTGOING_MISSED else CallBubbleType.INCOMING_MISSED
                    )
                } else {
                    // Active ringing call
                    CallState(
                        type = CallStateType.RINGING,
                        isVideo = isVideo,
                        isOutgoing = isOutgoing,
                        durationMs = null,
                        previewText = if (isOutgoing) {
                            if (isVideo) "Outgoing video call" else "Outgoing call"
                        } else {
                            if (isVideo) "Incoming video call" else "Incoming call"
                        },
                        bubbleType = if (isOutgoing) CallBubbleType.OUTGOING_RINGING else CallBubbleType.INCOMING_RINGING
                    )
                }
            }
        }
    }
}
