package io.getsafenow.libraries.gsn_matrix.api.room.knock

import io.getsafenow.libraries.gsn_matrix.api.core.EventId
import io.getsafenow.libraries.gsn_matrix.api.core.UserId


interface KnockRequest {
    val eventId: EventId
    val userId: UserId
    val displayName: String?
    val avatarUrl: String?
    val reason: String?
    val timestamp: Long?
    val isSeen: Boolean

    suspend fun accept(): Result<Unit>

    suspend fun decline(reason: String?): Result<Unit>

    suspend fun declineAndBan(reason: String?): Result<Unit>

    suspend fun markAsSeen(): Result<Unit>
}
