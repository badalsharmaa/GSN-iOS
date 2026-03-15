package com.example.getsafenowclient.common.events

import net.folivo.trixnity.client.store.TimelineEvent
import net.folivo.trixnity.core.model.events.m.call.CallEventContent
import net.folivo.trixnity.core.model.events.m.room.MemberEventContent
import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent

/**
 * Event filter – determines which events are allowed into the UI timeline flow.
 * 
 * We allow:
 * 1. Messages and Membership events.
 * 2. Call signaling (handled by call session builder).
 * 3. Encrypted / Unknown events (so they can be decrypted and re-emitted).
 */
val DefaultEventFilter: (TimelineEvent) -> Boolean = { e ->
    val content = e.event.content
    when (content) {
        is RoomMessageEventContent,
        is MemberEventContent,
        is CallEventContent -> true
        
        // Allow encrypted events through so Trixnity can decrypt them.
        // If we filter them here, the timeline will look empty until a manual reload.

        else -> {
            // Check if it's explicitly an encrypted type if needed, 
            // but usually content is null if not yet parsed/decrypted into a known type.
            true 
        }
    }
}
