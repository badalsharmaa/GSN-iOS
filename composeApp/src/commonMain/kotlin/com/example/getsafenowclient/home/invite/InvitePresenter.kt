package com.example.getsafenowclient.home.invite

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.example.getsafenowclient.component.InviteListItemData
import com.example.getsafenowclient.home.HomeViewScreenModel
import com.example.getsafenowclient.utils.getUserDisplayNameShort
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.client.room
import net.folivo.trixnity.client.room.getState
import net.folivo.trixnity.client.user
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.events.m.room.MemberEventContent

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun invitePresenter(
    homeViewScreenModel: HomeViewScreenModel,
    client: MatrixClient,
    onOpenRoom: (RoomId) -> Unit,
    onClose: () -> Unit
): Pair<InviteState, (InviteEvent) -> Unit> {

    val scope = rememberCoroutineScope()

    // Transform the flow of RoomHeaders into a flow of fully-resolved InviteListItemData.
    val invites by homeViewScreenModel.invites.flatMapLatest { roomHeaders ->
        if (roomHeaders.isEmpty()) {
            flowOf(emptyList())
        } else {
            val itemFlows = roomHeaders.map { roomHeader ->
                // For each RoomHeader, create a new flow that resolves the inviter's name.
                val roomId = roomHeader.id
                val inviterNameFlow: Flow<String> = client.room.getState<MemberEventContent>(roomId, stateKey = client.userId.full)
                    .flatMapLatest { inviteEvent ->
                        val inviterId = inviteEvent?.sender
                        if (inviterId != null) {
                            // We found the inviter's ID, now get their profile name for that room.
                            client.user.getById(roomId, inviterId).map { roomUser ->
                                getUserDisplayNameShort(roomUser, inviterId)
                            }
                        } else {
                            // Fallback to the room's title if we can't find the inviter.
                            flowOf(roomHeader.title)
                        }
                    }

                // Combine the resolved inviter name with the rest of the room data.
                inviterNameFlow.map { inviterName ->
                    InviteListItemData(
                        id = roomHeader.id.full,
                        inviterName = inviterName,
                        inviterAvatarUrl = roomHeader.avatarUrl?.toString(),
                        roomId = roomHeader.id.full
                    )
                }
            }
            // Combine all the individual item flows back into a single flow of a list.
            combine(itemFlows) { items -> items.toList() }
        }
    }.collectAsState(initial = emptyList())

    val eventSink: (InviteEvent) -> Unit = remember(onOpenRoom, onClose) {
        { ev ->
            when (ev) {
                is InviteEvent.AcceptInvite -> {
                    scope.launch {
                        runCatching {
                            client.api.room.joinRoom(RoomId(ev.roomId)).getOrThrow()
                        }.onSuccess {
                            onOpenRoom(RoomId(ev.roomId))
                        }.onFailure {
                            // TODO: surface error to user
                        }
                    }
                }

                is InviteEvent.DeclineInvite -> {
                    scope.launch {
                        runCatching {
                            client.api.room.leaveRoom(RoomId(ev.roomId)).getOrThrow()
                        }.onFailure {
                            // TODO: surface error to user
                        }
                    }
                }

                is InviteEvent.Refresh -> {
                    // No-op for now: flows are live
                }

                is InviteEvent.Close -> onClose()
            }
        }
    }

    val state = InviteState(
        invites = invites,
        isLoading = false, // Loading is handled by the HomeViewScreenModel now
        eventSink = eventSink
    )

    return state to eventSink
}
