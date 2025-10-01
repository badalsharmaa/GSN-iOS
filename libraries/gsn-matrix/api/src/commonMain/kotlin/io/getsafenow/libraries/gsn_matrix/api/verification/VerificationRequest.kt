package io.getsafenow.libraries.gsn_matrix.api.verification

import io.getsafenow.libraries.gsn_matrix.api.core.UserId
import kotlinx.serialization.Serializable

@Serializable
sealed interface VerificationRequest {
    sealed interface Outgoing : VerificationRequest {
        @Serializable
        data object CurrentSession : Outgoing

        @Serializable
        data class User(val userId: UserId) : Outgoing
    }
    @Serializable
    sealed class Incoming(open val details: SessionVerificationRequestDetails) : VerificationRequest {
        data class OtherSession(override val details: SessionVerificationRequestDetails) : Incoming(details)

        data class User(override val details: SessionVerificationRequestDetails) : Incoming(details)
    }
}
