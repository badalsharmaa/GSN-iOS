package io.getsafenow.libraries.gsn_matrix.api.auth


import io.getsafenow.libraries.gsn_matrix.api.GsnMClient
import io.getsafenow.libraries.gsn_matrix.api.auth.external.ExternalSession
import io.getsafenow.libraries.gsn_matrix.api.auth.qrlogin.MQrCodeLoginData
import io.getsafenow.libraries.gsn_matrix.api.auth.qrlogin.QrCodeLoginStep
import io.getsafenow.libraries.gsn_matrix.api.core.SessionId
import io.getsafenow.libraries.sessionstorage.api.LoggedInState

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MAuthenticationService {
    fun loggedInStateFlow(): Flow<LoggedInState>
    suspend fun getLatestSessionId(): SessionId?

    /**
     * Restore a session from a [sessionId].
     * Do not restore anything it the access token is not valid anymore.
     * Generally this method should not be used directly, prefer using [MClientProvider.getOrRestore] instead.
     */
    suspend fun restoreSession(sessionId: SessionId): Result<GsnMClient>
    fun getHomeserverDetails(): StateFlow<MHomeServerDetails?>
    suspend fun setHomeserver(homeserver: String): Result<Unit>
    suspend fun login(username: String, password: String): Result<SessionId>

    /**
     * Import a session that was created using another client, for instance Element Web.
     */
    suspend fun importCreatedSession(externalSession: ExternalSession): Result<SessionId>

    /*
     * OIDC part.
     */

    /**
     * Get the Oidc url to display to the user.
     */
    suspend fun getOidcUrl(
        prompt: OidcPrompt,
        loginHint: String?,
    ): Result<OidcDetails>

    /**
     * Cancel Oidc login sequence.
     */
    suspend fun cancelOidcLogin(): Result<Unit>

    /**
     * Attempt to login using the [callbackUrl] provided by the Oidc page.
     */
    suspend fun loginWithOidc(callbackUrl: String): Result<SessionId>

    suspend fun loginWithQrCode(qrCodeData: MQrCodeLoginData, progress: (QrCodeLoginStep) -> Unit): Result<SessionId>

    /** Listen to new Matrix clients being created on authentication. */
    fun listenToNewMatrixClients(lambda: (GsnMClient) -> Unit)
}
