package com.example.getsafenowclient.home.chat

import com.example.getsafenowclient.component.chat.UserSearchResult

/**
 * Defines all possible user interactions within the New Chat dialog.
 */
sealed interface NewChatEvent {
    data class SearchQueryChanged(val query: String) : NewChatEvent
    data class UserClicked(val user: UserSearchResult) : NewChatEvent
    object CloseDialog : NewChatEvent
}
