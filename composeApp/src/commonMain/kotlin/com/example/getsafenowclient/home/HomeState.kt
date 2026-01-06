package com.example.getsafenowclient.home

import com.example.getsafenowclient.component.ConversationListItemData
import com.example.getsafenowclient.home.chat.NewChatState

/**
 * Represents the complete state for the new home screen.
 */
data class HomeState(
    val userId: String = "",
    val userName: String = "",
    val userAvatarUrl: String? = null,
    val conversations: List<ConversationListItemData> = emptyList(),
    val isLoading: Boolean = true,
    val hasNewInvites: Boolean = false,

    // State for the "Start New Chat" dialog
    val newChatState: NewChatState = NewChatState(),
    val searchQuery: String
)
