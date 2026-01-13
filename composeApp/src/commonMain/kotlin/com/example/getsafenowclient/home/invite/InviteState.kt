package com.example.getsafenowclient.home.invite

import com.example.getsafenowclient.component.InviteListItemData

data class InviteState(
    val invites: List<InviteListItemData> = emptyList(),
    val isLoading: Boolean = false,
    val eventSink: (InviteEvent) -> Unit = {},
)
