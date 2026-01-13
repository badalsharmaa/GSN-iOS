package io.getsafenow.libraries.gsn_matrix.api.permalink

import androidx.compose.runtime.Immutable
import io.getsafenow.libraries.gsn_matrix.api.core.EventId
import io.getsafenow.libraries.gsn_matrix.api.core.RoomId
import io.getsafenow.libraries.gsn_matrix.api.core.RoomIdOrAlias
import io.getsafenow.libraries.gsn_matrix.api.core.UserId
import io.getsafenow.libraries.kmputils.platformkmp.PlatformUri
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf


/**
 * This sealed class represents all the permalink cases.
 * You don't have to instantiate yourself but should use [PermalinkParser] instead.
 */
/**
 * Represents all supported permalink cases used by GetSafeNow.
 * Use `PermalinkParser` to create instances.
 */
@Immutable
sealed interface PermalinkData {
    data class RoomLink(
        val roomIdOrAlias: RoomIdOrAlias,
        val eventId: EventId? = null,
        val viaParameters: ImmutableList<String> = persistentListOf(),
    ) : PermalinkData

    /*
     * Email invite extra params:
     * &room_name=OpsTeam
     * &room_avatar_url=mxc:
     * &inviter_name=dispatcher
     */
    data class RoomEmailInviteLink(
        val roomId: RoomId,
        val email: String,
        val signUrl: String,
        val roomName: String?,
        val roomAvatarUrl: String?,
        val inviterName: String?,
        val identityServer: String,
        val token: String,
        val privateKey: String,
        val roomType: String?,
    ) : PermalinkData

    data class UserLink(val userId: UserId) : PermalinkData

    /**
     * Any link that doesn’t match supported patterns but still needs to be handled.
     * For legacy group links, set `isLegacyGroupLink = true`.
     */
    data class FallbackLink(val uri: PlatformUri, val isLegacyGroupLink: Boolean = false) : PermalinkData
}
