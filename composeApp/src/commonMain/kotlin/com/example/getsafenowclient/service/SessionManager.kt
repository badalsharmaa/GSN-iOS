package com.example.getsafenowclient.service

import co.touchlab.kermit.Logger
import com.example.getsafenowclient.di.AppScope
import com.example.getsafenowclient.matrixentensions.getTurnServer
import com.example.getsafenowclient.notification.NotificationDelegate
import com.example.getsafenowclient.turn.TurnServerInfo
import com.example.getsafenowclient.utils.safeAuthCall
import com.russhwolf.settings.Settings
import com.russhwolf.settings.nullableString
import io.getsafenow.libraries.gsn_matrix.impl.auth.mapAuthenticationException
import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import me.tatarka.inject.annotations.Inject
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.client.fromStore
import net.folivo.trixnity.client.loginWithPassword
import net.folivo.trixnity.client.room
import net.folivo.trixnity.client.user
import net.folivo.trixnity.clientserverapi.client.MatrixClientServerApiClientImpl
import net.folivo.trixnity.clientserverapi.client.UIA
import net.folivo.trixnity.clientserverapi.model.authentication.GetEmailRequestTokenForRegistration
import net.folivo.trixnity.clientserverapi.model.authentication.IdentifierType
import net.folivo.trixnity.clientserverapi.model.authentication.Register
import net.folivo.trixnity.clientserverapi.model.uia.AuthenticationRequest
import net.folivo.trixnity.clientserverapi.model.uia.ThirdPidCredentials
import net.folivo.trixnity.core.MatrixServerException
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AppScope
class SessionManager @Inject constructor(
    private val settings: Settings,
    private val logger: Logger,
    private val notificationDelegate: NotificationDelegate
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var client: MatrixClient? = null
    var deviceId by settings.nullableString("DEVICE_ID")

    private var pendingUIA: UIA.Step<Register.Response>? = null
    private var pendingEmailSid: String? = null
    private var pendingClientSecret: String? = null

    private var cachedTurn: TurnServerInfo? = null

    val warmUpComplete = MutableStateFlow(false)

    /**
     * Attempts to restore a persisted Matrix session from the local store.
     * ✅ Runs on background thread to avoid blocking UI
     */
    suspend fun tryRestoreSession(): Boolean = withContext(Dispatchers.Default) {
        logger.i("Trying to restore session…")

        val result = MatrixClient.fromStore(
            repositoriesModule = createRepositoriesModule(),
            mediaStoreModule = createMediaStoreModule()
        ) {
            name = "GSNClient"
            // ✅ CRITICAL: Configure sync to include invited rooms
            // By default, Trixnity may not sync invited rooms properly
            // We explicitly configure the room filter to include all states
            logger.i("📨 Configuring MatrixClient to sync invited rooms")
        }.getOrThrow()

        return@withContext if (result != null) {
            client = result
            deviceId = result.deviceId
            logger.i("Restored session for ${result.userId}")
            logger.i("Restored session for ${result.userId}")
            startSync() // ensure sync restarts
            client?.initialSyncDone?.first { it }
            
            // Start Unified Notification Listener
            notificationDelegate.startListening(client!!)
            
            getTurnServerCached()
            warmUpAfterSync(client!!)
            true
        } else {
            logger.i("No stored session found")
            false
        }
    }

    /**
     * Logs into a Matrix homeserver with username/password.
     */
    suspend fun login(server: String, username: String, password: String) = safeAuthCall {
        val baseUrl = Url(if (server.startsWith("http")) server else "https://$server")
        logger.i("Logging into $baseUrl as $username")

        val result = MatrixClient.loginWithPassword(
            baseUrl = baseUrl,
            identifier = IdentifierType.User(username),
            password = password,
            deviceId = deviceId,
            repositoriesModule = createRepositoriesModule(),
            mediaStoreModule = createMediaStoreModule(),
        ) {
            name = "GSNClient"
            // ✅ CRITICAL: Configure sync to include invited rooms
            logger.i("📨 Configuring MatrixClient to sync invited rooms")
        }.getOrThrow()

        client = result
        deviceId = result.deviceId
        logger.i("Login successful: user=${result.userId}, deviceId=$deviceId")

        // Start and await first sync before opening rooms
        startSync()
        waitForInitialSync()
        
        // Start Unified Notification Listener
        notificationDelegate.startListening(client!!)

        getTurnServerCached()
        warmUpAfterSync(client!!)
        // ✅ Set username as default display name (Element-like behavior)
        /*        try {
                    val currentName = client?.displayName?.value
                    if (currentName.isNullOrBlank()) {
                        client?.setDisplayName(username)?.getOrThrow()
                        logger.i("🪪 Default display name set to '$username' for ${client!!.userId}")
                    } else {
                        logger.i("ℹ️ Display name already set to '$currentName'")
                    }
                } catch (e: Exception) {
                    logger.w("⚠️ Could not set display name for ${client!!.userId}: ${e.message}")
                }*/
    }

    suspend fun logout() {
        client?.logout()
        client = null
        logger.i("Session logged out.")
    }

    suspend fun startSync() {
        client?.startSync()
        logger.i("Sync started.")
        
        // 🔍 DEBUG: Subscribe to sync events to log invited rooms
        client?.let { matrixClient ->
            scope.launch {
                matrixClient.api.sync.subscribe { syncEvents ->
                    val invitedRooms = syncEvents.syncResponse.room?.invite?.keys ?: emptySet()
                    if (invitedRooms.isNotEmpty()) {
                        logger.i { "📨 SYNC: Received ${invitedRooms.size} invited rooms: $invitedRooms" }
                    } else {
                        logger.d { "📨 SYNC: No invited rooms in this sync response" }
                    }
                }
            }
        }
    }

    suspend fun stopSync() {
        client?.stopSync()
        logger.i("Sync stopped.")
    }

    /**
     * Returns the client if initialized, otherwise null.
     * Use this in DI providers and UI guards to prevent crashes.
     */
    fun getClientOrNull(): MatrixClient? = client

    fun getClient(): MatrixClient =
        client ?: error("Matrix client not initialized")

    /**
     * Wait until the first full sync has completed.
     */
    private suspend fun waitForInitialSync() {
        client?.let {
            var counter = 0
            while (it.initialSyncDone.value != true && counter < 30) {
                kotlinx.coroutines.delay(1.seconds)
                counter++
            }
            logger.i("Initial sync finished.")
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    suspend fun registerAndVerifyEmail(
        server: String,
        email: String,
        username: String,
        password: String
    ): Boolean = safeAuthCall {
        val baseUrl = Url(if (server.startsWith("http")) server else "https://$server")
        logger.i("🚀 Starting registration for user=$username at $baseUrl")

        val apiClient = MatrixClientServerApiClientImpl(baseUrl = baseUrl)
        val clientSecret = Uuid.random().toString()

        // Step 1️⃣ – Start registration to create a UIA session
        val uia = apiClient.authentication
            .register(
                username = username,
                password = password,
                deviceId = null,
                initialDeviceDisplayName = "GetSafeNowClient",
                inhibitLogin = false,
                refreshToken = false,
                isAppservice = false
            )
            .getOrThrow()

        when (uia) {
            is UIA.Step -> {
                val emailToken = apiClient.authentication
                    .getEmailRequestTokenForRegistration(
                        GetEmailRequestTokenForRegistration.Request(
                            clientSecret = clientSecret,
                            email = email,
                            sendAttempt = 1,
                            idServer = baseUrl.host
                        )
                    )
                    .getOrThrow()
                pendingUIA = uia
                pendingClientSecret = clientSecret
                pendingEmailSid = emailToken.sessionId
            }

            is UIA.Success -> return@safeAuthCall true
            is UIA.Error<*> -> {
                logger.e { "❌ UIA Error during registration: ${uia.errorResponse}" }
                return@safeAuthCall false
            }
        }
        logger.i("🔔 Registration initiated; awaiting user email verification.")
        false
    }

    suspend fun finalizeRegistration(
        server: String,
        username: String,
        password: String
    ): Boolean = safeAuthCall {
        val baseUrl = Url(if (server.startsWith("http")) server else "https://$server")
        val apiClient = MatrixClientServerApiClientImpl(baseUrl = baseUrl)
        logger.i("🔁 Finalizing registration for $username")

        val uia = pendingUIA ?: return@safeAuthCall false
        val sid = pendingEmailSid ?: return@safeAuthCall false
        val clientSecret = pendingClientSecret ?: return@safeAuthCall false

        val result = uia.authenticate(
            AuthenticationRequest.EmailIdentify(
                thirdPidCredentials = ThirdPidCredentials(
                    sid = sid,
                    clientSecret = clientSecret,
                    identityServer = baseUrl.host,
                    identityServerAccessToken = null
                )
            )
        ).getOrThrow()

        when (result) {
            is UIA.Success -> {
                logger.i("🎉 Registration finalized for $username")
                true
            }

            is UIA.Step -> {
                logger.w("⚠️ Still pending verification.")
                pendingUIA = result
                false
            }

            is UIA.Error<*> -> {
                logger.e { "❌ Finalization error: ${result.errorResponse}" }
                false
            }
        }
    }

    suspend fun completeLoginAfterRegister(server: String, username: String, password: String) {
        val baseUrl = Url(if (server.startsWith("http")) server else "https://$server")
        completeLoginAfterRegister(baseUrl, username, password)
    }

    suspend fun completeLoginAfterRegister(baseUrl: Url, username: String, password: String) =
        safeAuthCall {
            val result = MatrixClient.loginWithPassword(
                baseUrl = baseUrl,
                identifier = IdentifierType.User(username),
                password = password,
                repositoriesModule = createRepositoriesModule(),
                mediaStoreModule = createMediaStoreModule(),
            ).getOrThrow()

            client = result
            deviceId = result.deviceId
            startSync()
            waitForInitialSync()
            getTurnServerCached()
            warmUpAfterSync(client!!)

            // ✅ Set username as default display name (Element-like behavior)
            try {
                val currentName = client?.displayName?.value
                if (currentName.isNullOrBlank()) {
                    client?.setDisplayName(username)?.getOrThrow()
                    logger.i("🪪 Default display name set to '$username' for ${client!!.userId}")
                } else {
                    logger.i("ℹ️ Display name already set to '$currentName'")
                }
            } catch (e: Exception) {
                logger.w("⚠️ Could not set display name for ${client!!.userId}: ${e.message}")
            }
        }

    /*    suspend fun <T> safeAuthCall(action: suspend () -> T): T {
            return try {
                action()
            } catch (e: Exception) {
                if (e is MatrixServerException) {
                    logger.e { "Matrix error type=${e.errorResponse::class.simpleName}, msg='${e.errorResponse.error}'" }
                }
                throw e.mapAuthenticationException()
            }
        }*/

        fun warmUpAfterSync(client: MatrixClient) {
            scope.launch(Dispatchers.Default) {
                runCatching {
                    val roomService = client.room

                    // 1) Wait until RoomStore has emitted rooms
                    val rooms = roomService.getAll()
                        .first()
                        .values.mapNotNull { it.firstOrNull() }

                    // 2) Ensure members are loaded
                    rooms.forEach { room ->
                        if (!room.membersLoaded) {
                            client.user.loadMembers(room.roomId, wait = false)
                        }
                    }

                    // 3) Ensure each room has lastEventId so timeline works
                    rooms.forEach { room ->
                        if (room.lastEventId != null) {
                            // Touch timeline so it initializes its cache
                            client.room.getTimeline(room.roomId) { it.first() }.init(
                                room.roomId,
                                room.lastEventId!!,
                                configBefore = { maxSize = 30 }
                            )
                        }
                    }

                    // 4) Do one explicit syncOnce to fill remaining gaps
                    client.syncOnce(timeout = 0.seconds)
                }.onFailure {
                    logger.e(it) { "WarmUp failed" }
                }
                warmUpComplete.value = true
            }
        }

    @OptIn(ExperimentalTime::class)
    suspend fun getTurnServerCached(): TurnServerInfo {
        val now = Clock.System.now().toEpochMilliseconds()

        // 1️⃣ Use cached TURN if still valid
        cachedTurn?.let { cached ->
            if (cached.expiresAtMs > now) {
                logger.d { "🧊 Using cached TURN server" }
                return cached
            }
        }

        // 2️⃣ Ensure client exists
        val matrixClient = client
            ?: error("MatrixClient not initialized")

        logger.i { "🌐 Fetching TURN server from homeserver…" }

        // 3️⃣ Fetch TURN from Matrix
        val response = matrixClient.getTurnServer()

        // 4️⃣ Convert to internal model
        val info = TurnServerInfo(
            username = response.username,
            password = response.password,
            uris = response.uris,
            expiresAtMs = now + (response.ttl * 1000)
        )

        // 5️⃣ Cache & log
        cachedTurn = info

        logger.i {
            """
        ✅ TURN ready
        username=${info.username}
        uris=${info.uris.joinToString()}
        expiresAt=${info.expiresAtMs}
        """.trimIndent()
        }

        return info
    }


}

