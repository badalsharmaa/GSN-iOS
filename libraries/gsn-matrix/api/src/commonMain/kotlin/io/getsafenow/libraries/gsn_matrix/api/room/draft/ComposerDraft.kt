package io.getsafenow.libraries.gsn_matrix.api.room.draft

/**
 * A draft of a message composed by the user.
 * @param plainText The draft content in plain text.
 * @param htmlText If the message is formatted in HTML, the HTML representation of the message.
 * @param draftType The type of draft.
 */
data class ComposerDraft(
    val plainText: String,
    val htmlText: String?,
    val draftType: ComposerDraftType
)
