package io.getsafenow.libraries.gsn_matrix.api.auth.external

/***
 * Represents a client session data of a session created by another client.
 */
data class ExternalSession(
    val clientId: String,
    val deviceId: String,
    val accessToken: String,
    val refreshToken: String?,
    val clientServerUrl: String,
    val slidingSyncProxy: String?
)
