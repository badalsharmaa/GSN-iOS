package io.getsafenow.libraries.gsn_matrix.api.encryption.identity

import io.getsafenow.libraries.gsn_matrix.api.core.UserId


data class IdentityStateChange(
    val userId: UserId,
    val identityState: IdentityState,
)
