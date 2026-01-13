package io.getsafenow.libraries.gsn_matrix.api.encryption

sealed interface EnableRecoveryProgress {
    data object Starting : EnableRecoveryProgress
    data object CreatingBackup : EnableRecoveryProgress
    data object CreatingRecoveryKey : EnableRecoveryProgress
    data class BackingUp(val backedUpCount: Int, val totalCount: Int) : EnableRecoveryProgress
    data object RoomKeyUploadError : EnableRecoveryProgress
    data class Done(val recoveryKey: String) : EnableRecoveryProgress
}
