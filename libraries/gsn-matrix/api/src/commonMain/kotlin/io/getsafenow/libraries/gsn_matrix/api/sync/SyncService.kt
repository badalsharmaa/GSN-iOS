package io.getsafenow.libraries.gsn_matrix.api.sync

import kotlinx.coroutines.flow.StateFlow

interface SyncService {
    /**
     * Tries to start the sync. If already syncing it has no effect.
     */
    suspend fun startSync(): Result<Unit>

    /**
     * Tries to stop the sync. If service is not syncing it has no effect.
     */
    suspend fun stopSync(): Result<Unit>

    /**
     * Flow of [SyncState]. Will be updated as soon as the current [SyncState] changes.
     */
    val syncState: StateFlow<SyncState>

    val isOnline: StateFlow<Boolean>
}
