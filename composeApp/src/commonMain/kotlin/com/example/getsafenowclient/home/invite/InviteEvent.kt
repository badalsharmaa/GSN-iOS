package com.example.getsafenowclient.home.invite

sealed interface InviteEvent {
    data class AcceptInvite(val roomId: String) : InviteEvent
    data class DeclineInvite(val roomId: String) : InviteEvent
    object Refresh : InviteEvent
    object Close : InviteEvent
}
