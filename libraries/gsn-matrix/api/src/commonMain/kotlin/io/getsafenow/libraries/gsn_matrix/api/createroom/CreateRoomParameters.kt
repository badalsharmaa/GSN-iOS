package io.getsafenow.libraries.gsn_matrix.api.createroom

import io.getsafenow.libraries.gsn_matrix.api.core.UserId
import io.getsafenow.libraries.kmputils.platformkmp.KmpOptional


data class CreateRoomParameters(
    val name: String?,
    val topic: String? = null,
    val isEncrypted: Boolean,
    val isDirect: Boolean = false,
    val visibility: RoomVisibility,
    val preset: RoomPreset,
    val invite: List<UserId>? = null,
    val avatar: String? = null,
    val joinRuleOverride: JoinRule? = null,
    val historyVisibilityOverride: RoomHistoryVisibility? = null,
    val roomAliasName: KmpOptional<String> = KmpOptional.empty(),
)
