package io.getsafenow.libraries.gsn_matrix.api

import io.getsafenow.libraries.gsn_matrix.api.core.SessionId


interface MClientProvider {
    /**
     * Can be used to get or restore a MatrixClient with the given [SessionId].
     * If a [MatrixClient] is already in memory, it'll return it. Otherwise it'll try to restore one.
     * Most of the time you want to use injected constructor instead of retrieving a MatrixClient with this provider.
     */
    suspend fun getOrRestore(sessionId: SessionId): Result<GsnMClient>

    /**
     * Can be used to retrieve an existing [MatrixClient] with the given [SessionId].
     * @param sessionId the [SessionId] of the [MatrixClient] to retrieve.
     * @return the [MatrixClient] if it exists.
     */
    fun getOrNull(sessionId: SessionId): GsnMClient?
}
