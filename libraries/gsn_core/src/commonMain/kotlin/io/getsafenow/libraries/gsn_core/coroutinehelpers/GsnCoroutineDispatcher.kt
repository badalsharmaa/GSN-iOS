package io.getsafenow.libraries.gsn_core.coroutinehelpers

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Defines dispatchers used across the app.
 *
 * - [io]: for network / database / file operations
 * - [computation]: for CPU-intensive work
 * - [main]: for UI updates (main thread)
 */
data class GsnCoroutineDispatcher(
    val io: CoroutineDispatcher,
    val computation: CoroutineDispatcher,
    val main: CoroutineDispatcher,
)
