/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.getsafenow.libraries.gsn_matrix.impl.auth

/*
import co.touchlab.kermit.Logger
import io.getsafenow.libraries.di.AppScopeGsn
import io.getsafenow.libraries.gsn_core.coroutinehelpers.GsnCoroutineDispatcher
import io.getsafenow.libraries.gsn_core.extensionshelper.mapFailure
import io.getsafenow.libraries.gsn_core.extensionshelper.runCatchingExceptions
import io.getsafenow.libraries.gsn_matrix.api.GsnMClient
import io.getsafenow.libraries.gsn_matrix.api.auth.MAuthenticationService
import io.getsafenow.libraries.gsn_matrix.api.auth.MHomeServerDetails
import io.getsafenow.libraries.gsn_matrix.api.auth.qrlogin.MQrCodeLoginData
import io.getsafenow.libraries.gsn_matrix.api.auth.qrlogin.QrCodeLoginStep
import io.getsafenow.libraries.gsn_matrix.api.core.SessionId
import io.getsafenow.libraries.gsn_matrix.impl.paths.ClientSessionPaths
import io.getsafenow.libraries.gsn_matrix.impl.paths.ClientSessionPathsFactory
import io.getsafenow.libraries.sessionstorage.api.ClientSessionStore
import io.getsafenow.libraries.sessionstorage.api.LoggedInState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject
import software.amazon.lastmile.kotlin.inject.anvil.ContributesBinding
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn


@ContributesBinding(AppScopeGsn::class)
@SingleIn(AppScopeGsn::class)
class RustMatrixAuthenticationService @Inject constructor(
    private val sessionPathsFactory: ClientSessionPathsFactory,
    private val coroutineDispatchers: GsnCoroutineDispatcher,
    private val sessionStore: ClientSessionStore,
    private val rustMatrixClientFactory: RustMatrixClientFactory,
    private val passphraseGenerator: PassphraseGenerator,
    private val oidcConfigurationProvider: OidcConfigurationProvider,
) : MAuthenticationService {
    // Passphrase which will be used for new sessions. Existing sessions will use the passphrase
    // stored in the SessionData.
    private val pendingPassphrase = getDatabasePassphrase()

    // Need to keep a copy of the current session path to eventually delete it.
    // Ideally it would be possible to get the sessionPath from the Client to avoid doing this.
    private var sessionPaths: ClientSessionPaths? = null
    private var currentClient: Client? = null
    private var currentHomeserver = MutableStateFlow<MHomeServerDetails?>(null)

    private val newMatrixClientObservers = mutableListOf<(GsnMClient) -> Unit>()
    override fun listenToNewMatrixClients(lambda: (GsnMClient) -> Unit) {
        newMatrixClientObservers.add(lambda)
    }

    private fun rotateSessionPath(): ClientSessionPaths {
        sessionPaths?.deleteRecursively()
        return sessionPathsFactory.create()
            .also { sessionPaths = it }
    }

    override fun loggedInStateFlow(): Flow<LoggedInState> {
        return sessionStore.isLoggedIn()
    }

    override suspend fun getLatestSessionId(): SessionId? = withContext(coroutineDispatchers.io) {
        sessionStore.getLatestSession()?.clientId?.let { SessionId(it) }
    }

    override suspend fun restoreSession(sessionId: SessionId): Result<GsnMClient> = withContext(coroutineDispatchers.io) {
        runCatchingExceptions {
            val sessionData = sessionStore.getSession(sessionId.value)
            if (sessionData != null) {
                if (sessionData.isTokenValid) {
                    // Use the sessionData.passphrase, which can be null for a previously created session
                    if (sessionData.securityPhase == null) {
                        Logger.w("Restoring a session without a passphrase")
                    } else {
                        Logger.w("Restoring a session with a passphrase")
                    }
                    rustMatrixClientFactory.create(sessionData)
                } else {
                    error("Token is not valid")
                }
            } else {
                error("No session to restore with id $sessionId")
            }
        }.mapFailure { failure ->
            failure.mapClientException()
        }
    }

    private fun getDatabasePassphrase(): String? {
        val passphrase = passphraseGenerator.generatePassphrase()
        if (passphrase != null) {
            Timber.w("New sessions will be encrypted with a passphrase")
        }
        return passphrase
    }

    override fun getHomeserverDetails(): StateFlow<MatrixHomeServerDetails?> = currentHomeserver

    override suspend fun setHomeserver(homeserver: String): Result<Unit> =
        withContext(coroutineDispatchers.io) {
            val emptySessionPath = rotateSessionPath()
            runCatchingExceptions {
                val client = makeClient(sessionPaths = emptySessionPath) {
                    serverNameOrHomeserverUrl(homeserver)
                }

                currentClient = client
                val homeServerDetails = client.homeserverLoginDetails().map()
                currentHomeserver.value = homeServerDetails.copy(url = homeserver)
            }.onFailure {
                clear()
            }.mapFailure { failure ->
                Timber.e(failure, "Failed to set homeserver to $homeserver")
                failure.mapAuthenticationException()
            }
        }

    override suspend fun login(username: String, password: String): Result<SessionId> =
        withContext(coroutineDispatchers.io) {
            runCatchingExceptions {
                val client = currentClient ?: error("You need to call `setHomeserver()` first")
                val currentSessionPaths = sessionPaths ?: error("You need to call `setHomeserver()` first")
                client.login(username, password, "Element X Android", null)
                val sessionData = client.session()
                    .toSessionData(
                        isTokenValid = true,
                        loginType = LoginType.PASSWORD,
                        passphrase = pendingPassphrase,
                        sessionPaths = currentSessionPaths,
                    )
                val matrixClient = rustMatrixClientFactory.create(client)
                newMatrixClientObservers.forEach { it.invoke(matrixClient) }
                sessionStore.storeData(sessionData)

                // Clean up the strong reference held here since it's no longer necessary
                currentClient = null

                SessionId(sessionData.userId)
            }.mapFailure { failure ->
                Timber.e(failure, "Failed to login")
                failure.mapAuthenticationException()
            }
        }

    override suspend fun importCreatedSession(externalSession: ExternalSession): Result<SessionId> =
        withContext(coroutineDispatchers.io) {
            runCatchingExceptions {
                currentClient ?: error("You need to call `setHomeserver()` first")
                val currentSessionPaths = sessionPaths ?: error("You need to call `setHomeserver()` first")
                val sessionData = externalSession.toSessionData(
                    isTokenValid = true,
                    loginType = LoginType.PASSWORD,
                    passphrase = pendingPassphrase,
                    sessionPaths = currentSessionPaths,
                )
                clear()
                sessionStore.storeData(sessionData)
                SessionId(sessionData.userId)
            }
        }

    private var pendingOAuthAuthorizationData: OAuthAuthorizationData? = null

    override suspend fun getOidcUrl(
        prompt: OidcPrompt,
        loginHint: String?,
    ): Result<OidcDetails> {
        return withContext(coroutineDispatchers.io) {
            runCatchingExceptions {
                val client = currentClient ?: error("You need to call `setHomeserver()` first")
                val oAuthAuthorizationData = client.urlForOidc(
                    oidcConfiguration = oidcConfigurationProvider.get(),
                    prompt = prompt.toRustPrompt(),
                    loginHint = loginHint,
                    // If we want to restore a previous session for which we have encryption keys, we can pass the deviceId here. At the moment, we don't
                    deviceId = null,
                    additionalScopes = emptyList(),
                )
                val url = oAuthAuthorizationData.loginUrl()
                pendingOAuthAuthorizationData = oAuthAuthorizationData
                OidcDetails(url)
            }.mapFailure { failure ->
                Timber.e(failure, "Failed to get OIDC URL")
                failure.mapAuthenticationException()
            }
        }
    }

    override suspend fun cancelOidcLogin(): Result<Unit> {
        return withContext(coroutineDispatchers.io) {
            runCatchingExceptions {
                pendingOAuthAuthorizationData?.use {
                    currentClient?.abortOidcAuth(it)
                }
                pendingOAuthAuthorizationData = null
            }.mapFailure { failure ->
                Timber.e(failure, "Failed to cancel OIDC login")
                failure.mapAuthenticationException()
            }
        }
    }

    */
/**
     * callbackUrl should be the uriRedirect from OidcClientMetadata (with all the parameters).
     *//*

    override suspend fun loginWithOidc(callbackUrl: String): Result<SessionId> {
        return withContext(coroutineDispatchers.io) {
            runCatchingExceptions {
                val client = currentClient ?: error("You need to call `setHomeserver()` first")
                val currentSessionPaths = sessionPaths ?: error("You need to call `setHomeserver()` first")
                client.loginWithOidcCallback(callbackUrl)
                val sessionData = client.session().toSessionData(
                    isTokenValid = true,
                    loginType = LoginType.OIDC,
                    passphrase = pendingPassphrase,
                    sessionPaths = currentSessionPaths,
                )

                // Free the pending data since we won't use it to abort the flow anymore
                pendingOAuthAuthorizationData?.close()
                pendingOAuthAuthorizationData = null

                val matrixClient = rustMatrixClientFactory.create(client)
                newMatrixClientObservers.forEach { it.invoke(matrixClient) }
                sessionStore.storeData(sessionData)

                // Clean up the strong reference held here since it's no longer necessary
                currentClient = null

                SessionId(sessionData.userId)
            }.mapFailure { failure ->
                Logger.e("Failed to login with OIDC",failure )
                failure.mapAuthenticationException()
            }
        }
    }

    override suspend fun loginWithQrCode(qrCodeData: MQrCodeLoginData, progress: (QrCodeLoginStep) -> Unit) =
        withContext(coroutineDispatchers.io) {
            val sdkQrCodeLoginData = (qrCodeData as SdkQrCodeLoginData).rustQrCodeData
            val emptySessionPaths = rotateSessionPath()
            val oidcConfiguration = oidcConfigurationProvider.get()
            val progressListener = object : QrLoginProgressListener {
                override fun onUpdate(state: QrLoginProgress) {
                    Logger.d("QR Code login progress: $state")
                    progress(state.toStep())
                }
            }
            runCatchingExceptions {
                val client = makeQrCodeLoginClient(
                    sessionPaths = emptySessionPaths,
                    passphrase = pendingPassphrase,
                    qrCodeData = sdkQrCodeLoginData,
                )
                client.loginWithQrCode(
                    qrCodeData = qrCodeData.rustQrCodeData,
                    oidcConfiguration = oidcConfiguration,
                    progressListener = progressListener,
                )

                val sessionData = client.session()
                    .toSessionData(
                        isTokenValid = true,
                        loginType = LoginType.QR,
                        passphrase = pendingPassphrase,
                        sessionPaths = emptySessionPaths,
                    )
                val matrixClient = rustMatrixClientFactory.create(client)
                newMatrixClientObservers.forEach { it.invoke(matrixClient) }
                sessionStore.storeData(sessionData)

                // Clean up the strong reference held here since it's no longer necessary
                currentClient = null

                SessionId(sessionData.userId)
            }.mapFailure {
                when (it) {
                    is QrCodeDecodeException -> QrErrorMapper.map(it)
                    is HumanQrLoginException -> QrErrorMapper.map(it)
                    else -> it
                }
            }.onFailure { throwable ->
                if (throwable is CancellationException) {
                    throw throwable
                }
                Timber.e(throwable, "Failed to login with QR code")
            }
        }

    private suspend fun makeClient(
        sessionPaths: SessionPaths,
        config: suspend ClientBuilder.() -> ClientBuilder,
    ): Client {
        Timber.d("Creating client with simplified sliding sync")
        return rustMatrixClientFactory
            .getBaseClientBuilder(
                sessionPaths = sessionPaths,
                passphrase = pendingPassphrase,
                slidingSyncType = ClientBuilderSlidingSync.Discovered,
            )
            .config()
            .build()
    }

    private suspend fun makeQrCodeLoginClient(
        sessionPaths: SessionPaths,
        passphrase: String?,
        qrCodeData: QrCodeData,
    ): Client {
        Timber.d("Creating client for QR Code login with simplified sliding sync")
        return rustMatrixClientFactory
            .getBaseClientBuilder(
                sessionPaths = sessionPaths,
                passphrase = pendingPassphrase,
                slidingSyncType = ClientBuilderSlidingSync.Discovered,
            )
            .sessionPassphrase(passphrase)
            .serverNameOrHomeserverUrl(qrCodeData.serverName()!!)
            .build()
    }

    private fun clear() {
        currentClient?.close()
        currentClient = null
    }
}
*/
