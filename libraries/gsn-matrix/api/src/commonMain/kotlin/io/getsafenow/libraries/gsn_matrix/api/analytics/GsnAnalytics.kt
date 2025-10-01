package io.getsafenow.libraries.gsn_matrix.api.analytics

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Generic analytics client API used across the app. */
interface GsnAnalytics {
    fun setConsent(consented: Boolean)
    fun setUserId(userId: String?)              // optional: for logged-in users
    fun setSessionId(sessionId: String?)        // session-bound
    fun setUserProperty(key: String, value: String?)
    fun log(event: GsnEvent)
    fun flush()                                  // push buffered events now
}

/** Event base type */
@Serializable
sealed interface GsnEvent {
    val name: String
    val ts: Long               // unix millis in UTC
    val sessionId: String?
    val userId: String?
}

/** JoinRoom event */
@Serializable
@SerialName("JoinedRoom")
data class GsnJoinedRoomEvent(
    override val name: String = "JoinedRoom",
    override val ts: Long,
    override val sessionId: String?,
    override val userId: String?,
    val roomIdOrAlias: String,
    val serverNames: List<String>,
    val trigger: GsnJoinRoomTrigger,
    val roomSize: GsnRoomSize? = null,          // optional if known
) : GsnEvent