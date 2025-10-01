package io.getsafenow.libraries.gsn_matrix.api.encryption


import androidx.compose.runtime.Immutable

@Immutable
sealed interface BackupUploadState {
    data object Unknown : BackupUploadState

    data object Waiting : BackupUploadState

    data class Uploading(
        val backedUpCount: Int,
        val totalCount: Int,
    ) : BackupUploadState

    data object Done : BackupUploadState

    data object Error : BackupUploadState

    data class SteadyException(val exception: SteadyStateException) : BackupUploadState
}
