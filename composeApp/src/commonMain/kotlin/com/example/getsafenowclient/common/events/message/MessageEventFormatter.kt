package com.example.getsafenowclient.common.events.message

import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent
import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent.*
import net.folivo.trixnity.core.model.events.m.room.bodyWithoutFallback

/**
 * Unified formatter for ALL Matrix message event types.
 * Used for:
 * - Home screen preview
 * - Timeline bubbles
 * - Notifications
 */
object MessageEventFormatter {

    // ----------------------------------------------------------
    // PREVIEW VERSION (Home list, notifications, search results)
    // ----------------------------------------------------------
    fun formatPreview(content: RoomMessageEventContent): String {
        return when (content) {

            // TEXT / EMOTE / NOTICE
            is TextBased.Text -> content.bodyWithoutFallback
            is TextBased.Notice -> content.bodyWithoutFallback
            is TextBased.Emote -> "• ${content.bodyWithoutFallback}"

            // FILE
            is FileBased.Image -> "📷 Photo"
            is FileBased.Video -> "🎥 Video"
            is FileBased.Audio -> "🎧 Audio"
            is FileBased.File -> "📄 File"

            // LOCATION
            is Location -> "📍 Location shared"

            // VERIFICATION
            is VerificationRequest -> "🔐 Verification request"

            // UNKNOWN MESSAGE
            is Unknown ->
                "[${content.type}]"

        }
    }

    // ----------------------------------------------------------
    // TIMELINE VERSION (Room message bubble text)
    // ----------------------------------------------------------
    fun formatTimeline(content: RoomMessageEventContent): String {
        return when (content) {

            // TEXT / EMOTE / NOTICE
            is TextBased.Text -> content.bodyWithoutFallback
            is TextBased.Notice -> content.bodyWithoutFallback
            is TextBased.Emote -> "• ${content.bodyWithoutFallback}"

            // FILE
            is FileBased.Image -> "🖼️ [image]"
            is FileBased.Video -> "🎥 [video]"
            is FileBased.Audio -> "🎧 [audio]"
            is FileBased.File ->
                if (content.fileName != null) "📄 [file: ${content.fileName}]"
                else "📄 [file]"

            // LOCATION
            is Location -> "📍 User shared a location"

            // VERIFICATION
            is VerificationRequest -> "🔐 Verification request"

            // UNKNOWN
            is RoomMessageEventContent.Unknown -> "[${content.type}]"

        }
    }
}
