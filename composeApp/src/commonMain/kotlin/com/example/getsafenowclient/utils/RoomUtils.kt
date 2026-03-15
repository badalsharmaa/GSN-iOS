package com.example.getsafenowclient.utils

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.client.room
import net.folivo.trixnity.client.room.RoomService
import net.folivo.trixnity.client.room.getAllState
import net.folivo.trixnity.client.store.Room
import net.folivo.trixnity.client.store.RoomUser
import net.folivo.trixnity.client.store.type
import net.folivo.trixnity.client.user
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.UserId
import net.folivo.trixnity.core.model.events.m.room.CreateEventContent
import net.folivo.trixnity.core.model.events.m.space.ChildEventContent
import net.folivo.trixnity.core.model.events.m.space.ParentEventContent

// ---------------------------------------------------------------------------
// Room hierarchy helpers
// ---------------------------------------------------------------------------

/**
 * Recursively builds a subtree of space → room relationships.
 */
suspend fun RoomService.getSubTree(rootId: RoomId): List<RoomId> {
    val result = mutableListOf<RoomId>()
    getChildren(rootId)
        .map { it to isSpace(it) }
        .sortedBy { (_, isSpace) -> isSpace } // sort spaces after rooms
        .forEach { (id, _) ->
            result.add(id)
            result.addAll(getSubTree(id))
        }
    return result
}

/** Check if the room is a Space. */
suspend fun RoomService.isSpace(roomId: RoomId): Boolean =
    getById(roomId).first()?.type == CreateEventContent.RoomType.Space

/** Check if the room is the root of a space tree (no parent space). */
suspend fun RoomService.isRoot(roomId: RoomId): Boolean =
    getAllState<ParentEventContent>(roomId).firstOrNull().isNullOrEmpty()

/** Get all child rooms/subspaces of a space. */
suspend fun RoomService.getChildren(roomId: RoomId): List<RoomId> =
    getAllState<ChildEventContent>(roomId)
        .firstOrNull()
        ?.keys
        ?.map { RoomId(it) }
        .orEmpty()

// ---------------------------------------------------------------------------
// Room name helpers
// ---------------------------------------------------------------------------

/**
 * Observe a readable room name, falling back to heroes or roomId if needed.
 * Works with RoomDisplayName(explicitName, heroes, otherUsersCount, isEmpty, summary)
 */
fun Room.nameFlow(client: MatrixClient): Flow<String> {
    val displayName = name ?: return flowOf(roomId.full)
    val heroes = displayName.heroes
    val fullName = displayName.explicitName

    fun nameFromHeroes(roomUser: RoomUser?, heroes: List<UserId>, index: Int): String =
        getUserDisplayName(roomUser, heroes[index])

    return when {
        !fullName.isNullOrEmpty() -> flowOf(fullName)
        heroes.isEmpty() -> flowOf(roomId.full)
        else -> combine(heroes.map { client.user.getById(roomId, it) }) { users ->
            val heroConcat = users.mapIndexed { index, roomUser ->
                when {
                    index < heroes.size - 2 -> nameFromHeroes(roomUser, heroes, index) + ", "
                    index == heroes.size - 2 -> nameFromHeroes(roomUser, heroes, index) + " and "
                    else -> nameFromHeroes(roomUser, heroes, index)
                }
            }.joinToString("")
            heroConcat
        }
    }
}

/** Fallback nameFlow that starts from RoomId. */
@OptIn(ExperimentalCoroutinesApi::class)
fun RoomId.nameFlow(client: MatrixClient): Flow<String> = flow {
    emitAll(
        client.room.getById(this@nameFlow)
            .flatMapLatest { it?.nameFlow(client) ?: flowOf(this@nameFlow.full) }
    )
}

// ---------------------------------------------------------------------------
// User name helpers
// ---------------------------------------------------------------------------

/**
 * Resolves a user's display name with consistent fallback logic.
 * Falls back to userId.full if RoomUser is null or has no name.
 */
fun getUserDisplayName(roomUser: RoomUser?, userId: UserId): String =
    roomUser?.name ?: userId.full

/**
 * Resolves a user's display name with fallback to localpart only.
 * Useful for more compact displays.
 */
fun getUserDisplayNameShort(roomUser: RoomUser?, userId: UserId): String =
    roomUser?.name ?: userId.localpart
