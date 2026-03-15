package com.example.getsafenowclient.service

import com.example.getsafenowclient.di.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import me.tatarka.inject.annotations.Inject
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.clientserverapi.client.SyncState

/**
 * Global sync state manager.
 * Provides a single source of truth for Matrix sync status across the entire app.
 */
@AppScope
class GlobalSyncManager @Inject constructor(
    private val sessionManager: SessionManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    /**
     * Indicates whether the Matrix SDK is currently syncing.
     * This will be true during INITIAL_SYNC and false otherwise.
     */
    val isSyncing: StateFlow<Boolean> = sessionManager.warmUpComplete.map { warmUpDone ->
        if (!warmUpDone) {
            // During warmup, check actual sync state
            val client = sessionManager.getClientOrNull()
            client?.syncState?.value == SyncState.INITIAL_SYNC
        } else {
            // After warmup, only show during explicit syncs
            val client = sessionManager.getClientOrNull()
            client?.syncState?.value == SyncState.INITIAL_SYNC
        }
    }.stateIn(scope, SharingStarted.Eagerly, false)
}
