package com.example.getsafenowclient.home.chat

import com.example.getsafenowclient.component.chat.UserSearchResult

/**
 * Represents the state specifically for the 'Start New Chat' dialog.
 */
data class NewChatState(
    val isVisible: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<UserSearchResult> = emptyList(),
    val isSearching: Boolean = false
)