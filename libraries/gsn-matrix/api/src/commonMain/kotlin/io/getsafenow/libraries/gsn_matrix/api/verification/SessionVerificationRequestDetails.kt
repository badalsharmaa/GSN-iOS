package io.getsafenow.libraries.gsn_matrix.api.verification

import io.getsafenow.libraries.gsn_matrix.api.core.DeviceId
import io.getsafenow.libraries.gsn_matrix.api.core.FlowId
import io.getsafenow.libraries.gsn_matrix.api.core.UserId
import kotlinx.serialization.Serializable

@Serializable
data class SessionVerificationRequestDetails(
    val senderProfile: SenderProfile,
    val flowId: FlowId,
    val deviceId: DeviceId,
    val firstSeenTimestamp: Long,
) {
    @Serializable
    data class SenderProfile(
        val userId: UserId,
        val displayName: String?,
        val avatarUrl: String?,
    )
}
