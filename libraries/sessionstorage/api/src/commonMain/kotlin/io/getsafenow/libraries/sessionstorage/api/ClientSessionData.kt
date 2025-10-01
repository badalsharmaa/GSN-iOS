package io.getsafenow.libraries.sessionstorage.api

import kotlin.time.ExperimentalTime


/**
 * Data class representing the session data to store locally.
 */
data class ClientSessionData @OptIn(ExperimentalTime::class) constructor(
    /** The client ID of the logged in client. */
    val clientId: String,
    /** The device ID of the client session. */
    val deviceId: String,
    /** The current access token of the client session. */
    val accessToken: String,
    /** The optional current refresh token of the client session. */
    val refreshToken: String?,
    /** The clientServer URL of the client session. */
    val clientServerUrl: String,
    /** The Open ID Connect info for this client session, if any. */
    val oidcData: String?,
    /** The Sliding Sync Proxy URL for this client session, if any. */
    val slidingSyncProxy: String?,
    /** The timestamp of the last login. May be null in very old client sessions. */
    val loginTimestamp: kotlin.time.Instant?,
    /** Whether the [accessToken] is valid or not. */
    val isTokenValid: Boolean,
    /** The login type used to authenticate the client session. */
    val gsnLoginType: GsnLoginType,
    /** The optional passphrase used to encrypt data in the SDK local store. */
    val securityPhase: String?,
    /** The paths to the client session data stored in the filesystem. */
    val clientSessionPath: String,
    /** The path to the cache data stored for the session in the filesystem. */
    val cachePath: String,
    )
