package io.getsafenow.libraries.gsn_matrix.api.encryption

import androidx.compose.runtime.Immutable

@Immutable
sealed interface SteadyStateException {
    /**
     * The backup can be deleted.
     */
    data class BackupDisabled(val message: String) : SteadyStateException

    /**
     * The task waiting for notifications coming from the upload task can fall behind so much that it lost some notifications.
     */
    data class Lagged(val message: String) : SteadyStateException

    /**
     * The request(s) to upload the room keys failed.
     */
    data class Connection(val message: String) : SteadyStateException
}
