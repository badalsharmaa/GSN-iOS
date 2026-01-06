package com.example.getsafenowclient.matrixentensions

import com.example.getsafenowclient.turn.GetTurnServer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.client.store.Room
import net.folivo.trixnity.client.store.TimelineEvent
import net.folivo.trixnity.client.user
import net.folivo.trixnity.clientserverapi.model.authentication.ThirdPartyIdentifier
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.UserId
import net.folivo.trixnity.core.model.events.m.DirectEventContent
import net.folivo.trixnity.clientserverapi.model.rooms.DirectoryVisibility
import net.folivo.trixnity.core.model.events.m.room.CreateEventContent
import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent

/**
 * Finds or creates a direct message (DM) room with a given user.
 * Fully compatible with your current Trixnity 4.22+ SDK.
 */
suspend fun MatrixClient.getOrCreateDirectRoom(targetUserId: UserId): Result<RoomId> = runCatching {
    // 1️⃣ Read existing account data of type m.direct
    val existingDirects: DirectEventContent? =
        user.getAccountData(DirectEventContent::class).first()

    // 2️⃣ Check for an existing mapping where this user is the key
    val existingRoomId = existingDirects
        ?.mappings
        ?.get(targetUserId)
        ?.firstOrNull()

    if (existingRoomId != null) return@runCatching existingRoomId

    // 3️⃣ Otherwise, create a new private direct room
    val newRoomId = api.room.createRoom(
        visibility = DirectoryVisibility.PRIVATE,
        invite = setOf(targetUserId),
        isDirect = true
    ).getOrThrow()

    // 4️⃣ Update the account data mapping
    val updatedMappings = existingDirects?.mappings?.toMutableMap() ?: mutableMapOf()
    updatedMappings[targetUserId] = setOf(newRoomId)

    // Use the low-level API to set account data (UserService has no setter)
    api.user.setAccountData(
        userId = userId,
        content = DirectEventContent(updatedMappings)
    ).getOrThrow()

    newRoomId
}

/**
 * Returns the list of room IDs that are marked as direct chats (1-to-1 DMs).
 */
suspend fun MatrixClient.getDirectRoomIds(): Set<RoomId> {
    val directData = user.getAccountData(DirectEventContent::class).first()
    return directData?.mappings?.values
        ?.filterNotNull()
        ?.flatten()
        ?.toSet()
        ?: emptySet()
}

/**
 * Returns the user ID of the peer in a direct (1-to-1) room, if known.
 */
suspend fun MatrixClient.getDirectUserForRoom(roomId: RoomId): UserId? {
    val directData = user.getAccountData(DirectEventContent::class).first()
    val entry = directData?.mappings?.entries?.firstOrNull { (_, rooms) ->
        rooms?.contains(roomId) == true
    }
    return entry?.key
}

fun MatrixClient.dmRoomToPeerMapFlow(): Flow<Map<RoomId, UserId>> =
    user.getAccountData(DirectEventContent::class)
        .map { directData ->
            val mappings = directData?.mappings.orEmpty()
            mappings.flatMap { (userId, rooms) ->
                rooms.orEmpty().map { roomId -> roomId to userId }
            }.toMap()
        }

suspend fun MatrixClient.getUserEmail(): String? {
    val ids = api.authentication.getThirdPartyIdentifiers().getOrThrow()

    return ids.firstOrNull { it.medium == ThirdPartyIdentifier.Medium.EMAIL }?.address
}

fun TimelineEvent.isRealMessage(): Boolean {
    val raw = content?.getOrNull()
    return raw is RoomMessageEventContent
}

suspend fun MatrixClient.getTurnServer(): GetTurnServer.Response {
    return api
        .baseClient
        .request(GetTurnServer, Unit)
        .getOrThrow()
}
