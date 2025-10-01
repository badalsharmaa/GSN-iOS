package io.getsafenow.libraries.gsn_matrix.api.room.join

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface JoinRule {
    data object Public : JoinRule
    data object Private : JoinRule
    data object Knock : JoinRule
    data object Invite : JoinRule
    data class Restricted(val rules: ImmutableList<AllowRule>) : JoinRule
    data class KnockRestricted(val rules: ImmutableList<AllowRule>) : JoinRule
    data class Custom(val value: String) : JoinRule
}
