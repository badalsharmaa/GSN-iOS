package io.getsafenow.libraries.gsn_matrix.api.user

import io.getsafenow.libraries.gsn_matrix.api.core.UserId
import kotlinx.serialization.Serializable

@Serializable
data class GsnMUser(
    val userId: UserId,
    val displayName: String? = null,
    val avatarUrl: String? = null,
)
