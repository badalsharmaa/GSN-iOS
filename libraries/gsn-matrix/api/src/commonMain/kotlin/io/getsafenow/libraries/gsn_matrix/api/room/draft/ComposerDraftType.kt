package io.getsafenow.libraries.gsn_matrix.api.room.draft

import io.getsafenow.libraries.gsn_matrix.api.core.EventId


sealed interface ComposerDraftType {
    data object NewMessage : ComposerDraftType
    data class Reply(val eventId: EventId) : ComposerDraftType
    data class Edit(val eventId: EventId) : ComposerDraftType
}
