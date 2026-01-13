package com.example.getsafenowclient.home

/**
 * Defines all possible user interactions on the new home screen.
 */
sealed interface HomeEvent {
    data class UpdateSearchQuery(val query: String) : HomeEvent
    data class ConversationClicked(val id: String) : HomeEvent
    data class FavoriteClicked(val id: String) : HomeEvent
    object FilterClicked : HomeEvent
    object AvatarClicked : HomeEvent
    object InviteClicked : HomeEvent


    // Events for the new chat flow
    object StartNewChatClicked : HomeEvent
    object CreateRoomClicked : HomeEvent
    object NewGroupChatClicked : HomeEvent
}
