package com.example.getsafenowclient.home.invite

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import kotlinx.coroutines.flow.map
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.client.flatten
import net.folivo.trixnity.client.room
import net.folivo.trixnity.client.store.Room
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.events.m.room.Membership

@Composable
fun rememberHasInvites(client: MatrixClient): Boolean {
    val hasInvites by produceState(initialValue = false, client) {
        client.room.getAll()
            .flatten() // Flow<Map<RoomId, Room?>>
            .map { roomsMap: Map<RoomId, Room?> ->
                roomsMap.values.any { room ->
                    room?.membership == Membership.INVITE
                }
            }
            .collect { value = it }
    }
    return hasInvites
}