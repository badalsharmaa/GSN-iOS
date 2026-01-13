package io.getsafenow.libraries.gsn_matrix.api.room

import io.getsafenow.libraries.gsn_matrix.api.GsnMClient
import io.getsafenow.libraries.gsn_matrix.api.core.RoomId
import io.getsafenow.libraries.gsn_matrix.api.core.UserId


/**
 * Try to find an existing DM with the given user, or create one if none exists and [createIfDmDoesNotExist] is true.
 */
suspend fun GsnMClient.startDM(
    userId: UserId,
    createIfDmDoesNotExist: Boolean,
): StartDMResult {
    return findDM(userId)
        .fold(
            onSuccess = { existingDM ->
                if (existingDM != null) {
                    StartDMResult.Success(existingDM, isNew = false)
                } else if (createIfDmDoesNotExist) {
                    createDM(userId).fold(
                        { StartDMResult.Success(it, isNew = true) },
                        { StartDMResult.Failure(it) }
                    )
                } else {
                    StartDMResult.DmDoesNotExist
                }
            },
            onFailure = { error ->
                StartDMResult.Failure(error)
            }
        )
}

sealed interface StartDMResult {
    data class Success(val roomId: RoomId, val isNew: Boolean) : StartDMResult
    data object DmDoesNotExist : StartDMResult
    data class Failure(val throwable: Throwable) : StartDMResult
}
