package io.getsafenow.libraries.gsn_matrix.api.room.preview

import io.getsafenow.libraries.gsn_matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.RoomType
import io.getsafenow.libraries.gsn_matrix.api.core.RoomAlias
import io.getsafenow.libraries.gsn_matrix.api.core.RoomId
import io.getsafenow.libraries.gsn_matrix.api.room.join.JoinRule


data class RoomPreviewInfo(
    /** The room id for this room. */
    val roomId: RoomId,
    /** The canonical alias for the room. */
    val canonicalAlias: RoomAlias?,
    /** The room's name, if set. */
    val name: String?,
    /** The room's topic, if set. */
    val topic: String?,
    /** The MXC URI to the room's avatar, if set. */
    val avatarUrl: String?,
    /** The number of joined members. */
    val numberOfJoinedMembers: Long,
    /** The room type (space, custom) or nothing, if it's a regular room. */
    val roomType: RoomType,
    /** Is the history world-readable for this room? */
    val isHistoryWorldReadable: Boolean,
    /** the membership of the current user. */
    val membership: CurrentUserMembership?,
    /** The room's join rule. */
    val joinRule: JoinRule?,
)
