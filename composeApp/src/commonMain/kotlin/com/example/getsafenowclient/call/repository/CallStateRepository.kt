package com.example.getsafenowclient.call.repository


import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Repository for persisting active call state across app restarts.
 * Stores incoming call data including SDP offer to prevent NULL errors.
 */
class CallStateRepository(
    private val dataStore: DataStore<Preferences>
) {
    
    companion object {
        private val ACTIVE_CALL_KEY = stringPreferencesKey("active_call_data")
    }
    
    /**
     * Save incoming call data to persistent storage.
     * This ensures call state survives app restarts and notification clicks.
     */
    suspend fun saveIncomingCall(callData: IncomingCallData) {
        dataStore.edit { preferences ->
            val json = Json.encodeToString(callData)
            preferences[ACTIVE_CALL_KEY] = json
        }
    }
    
    /**
     * Get active call data if exists.
     * Returns null if no active call or data is corrupted.
     */
    suspend fun getActiveCall(): IncomingCallData? {
        return try {
            dataStore.data.map { preferences ->
                preferences[ACTIVE_CALL_KEY]?.let { json ->
                    Json.decodeFromString<IncomingCallData>(json)
                }
            }.first()
        } catch (e: Exception) {
            co.touchlab.kermit.Logger.e(e) { "Failed to get active call data" }
            null
        }
    }
    
    /**
     * Clear all call state.
     * Should be called when call ends or is rejected.
     */
    suspend fun clearCall() {
        dataStore.edit { preferences ->
            preferences.remove(ACTIVE_CALL_KEY)
        }
    }
    
    /**
     * Check if there's an active call.
     */
    suspend fun hasActiveCall(): Boolean {
        return getActiveCall() != null
    }
}

/**
 * Data class representing an incoming call that needs to be persisted.
 */
@OptIn(ExperimentalTime::class)
@Serializable
data class IncomingCallData (
    val roomId: String,
    val callId: String,
    val offerSdp: String,
    val isVideo: Boolean,
    val opponentId: String,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds()
) {
    /**
     * Check if call data is still valid (not too old).
     * Calls older than 2 minutes should be considered expired.
     */
    fun isExpired(): Boolean {
        val twoMinutesMs = 2 * 60 * 1000
        return (Clock.System.now().toEpochMilliseconds() - timestamp) > twoMinutesMs
    }
    
    /**
     * Validate that SDP offer is not empty.
     */
    fun isValid(): Boolean {
        return offerSdp.isNotBlank() && 
               roomId.isNotBlank() && 
               callId.isNotBlank()
    }
}
