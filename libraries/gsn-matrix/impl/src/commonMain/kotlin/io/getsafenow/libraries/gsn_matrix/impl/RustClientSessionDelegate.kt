package io.getsafenow.libraries.gsn_matrix.impl


/*import io.getsafenow.libraries.gsn_core.coroutinehelpers.GsnCoroutineDispatcher
import io.getsafenow.libraries.gsn_core.log.logger.LoggerTag
import io.getsafenow.libraries.gsn_matrix.impl.paths.getSessionPaths
import io.getsafenow.libraries.sessionstorage.api.ClientSessionStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.matrix.rustcomponents.sdk.ClientDelegate
import org.matrix.rustcomponents.sdk.ClientSessionDelegate
import org.matrix.rustcomponents.sdk.Session
import timber.log.Timber
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

private val loggerTag = LoggerTag("RustClientSessionDelegate")

*//**
 * This class is responsible for handling the session data for the Rust SDK.
 *
 * It implements both [ClientSessionDelegate] and [ClientDelegate] to react to session data updates and auth errors.
 *
 * IMPORTANT: you must set the [client] property as soon as possible so [didReceiveAuthError] can work properly.
 *//*
class RustClientSessionDelegate(
    private val sessionStore: ClientSessionStore,
    private val appCoroutineScope: CoroutineScope,
    coroutineDispatchers: GsnCoroutineDispatcher,
) : ClientSessionDelegate, ClientDelegate {
    // Used to ensure several calls to `didReceiveAuthError` don't trigger multiple logouts
    @OptIn(ExperimentalAtomicApi::class)
    private val isLoggingOut = AtomicBoolean(false)

    // To make sure only one coroutine affecting the token persistence can run at a time
    private val updateTokensDispatcher = coroutineDispatchers.io.limitedParallelism(1)

    // This Client needs to be set up as soon as possible so `didReceiveAuthError` can work properly.
    private var client: WeakReference<RustMatrixClient> = WeakReference(null)

    *//**
     * Sets the [ClientDelegate] for the [RustMatrixClient], and keeps a reference to the client so it can be used later.
     *//*
    fun bindClient(client: RustMatrixClient) {
        this.client = WeakReference(client)
    }

    *//**
     * Clears the current client reference.
     *//*
    fun clearCurrentClient() {
        this.client.clear()
    }

    override fun saveSessionInKeychain(session: Session) {
        appCoroutineScope.launch(updateTokensDispatcher) {
            val existingData = sessionStore.getSession(session.clientId) ?: return@launch
            val (anonymizedAccessToken, anonymizedRefreshToken) = session.anonymizedTokens()
            Timber.tag(loggerTag.value).d(
                "Saving new session data with token: access token '$anonymizedAccessToken' and refresh token '$anonymizedRefreshToken'. " +
                    "Was token valid: ${existingData.isTokenValid}"
            )
            val newData = session.toSessionData(
                isTokenValid = true,
                loginType = existingData.gsnLoginType,
                passphrase = existingData.securityPhase,
                sessionPaths = existingData.getSessionPaths(),
            )
            sessionStore.updateData(newData)
            Timber.tag(loggerTag.value).d("Saved new session data with access token: '$anonymizedAccessToken'.")
        }.invokeOnCompletion {
            if (it != null) {
                Timber.tag(loggerTag.value).e(it, "Failed to save new session data.")
            }
        }
    }

    override fun didReceiveAuthError(isSoftLogout: Boolean) {
        Timber.tag(loggerTag.value).w("didReceiveAuthError(isSoftLogout=$isSoftLogout)")
        if (isLoggingOut.getAndSet(true).not()) {
            Timber.tag(loggerTag.value).v("didReceiveAuthError -> do the cleanup")
            // TODO handle isSoftLogout parameter.
            appCoroutineScope.launch(updateTokensDispatcher) {
                val currentClient = client.get()
                if (currentClient == null) {
                    Timber.tag(loggerTag.value).w("didReceiveAuthError -> no client, exiting")
                    isLoggingOut.set(false)
                    return@launch
                }
                val existingData = sessionStore.getSession(currentClient.sessionId.value)
                val (anonymizedAccessToken, anonymizedRefreshToken) = existingData.anonymizedTokens()
                Timber.tag(loggerTag.value).d(
                    "Removing session data with access token '$anonymizedAccessToken' " +
                        "and refresh token '$anonymizedRefreshToken'."
                )
                if (existingData != null) {
                    // Set isTokenValid to false
                    val newData = existingData.copy(isTokenValid = false)
                    sessionStore.updateData(newData)
                    Timber.tag(loggerTag.value).d("Invalidated session data with access token: '$anonymizedAccessToken'.")
                } else {
                    Timber.tag(loggerTag.value).d("No session data found.")
                }
                currentClient.logout(userInitiated = false, ignoreSdkError = true)
            }.invokeOnCompletion {
                if (it != null) {
                    Timber.tag(loggerTag.value).e(it, "Failed to remove session data.")
                }
            }
        } else {
            Timber.tag(loggerTag.value).v("didReceiveAuthError -> already cleaning up")
        }
    }

    override fun retrieveSessionFromKeychain(userId: String): Session {
        // This should never be called, as it's only used for multi-process setups
        error("retrieveSessionFromKeychain should never be called for Android")
    }
}*/
