
package io.getsafenow.libraries.sessionstorage.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface ClientSessionStore {
    fun isLoggedIn(): Flow<LoggedInState>
    fun sessionsFlow(): Flow<List<ClientSessionData>>
    suspend fun storeData(sessionData: ClientSessionData)

    /**
     * Will update the session data matching the userId, except the value of loginTimestamp.
     * No op if userId is not found in DB.
     */
    suspend fun updateData(sessionData: ClientSessionData)
    suspend fun getSession(sessionId: String): ClientSessionData?
    suspend fun getAllSessions(): List<ClientSessionData>
    suspend fun getLatestSession(): ClientSessionData?
    suspend fun removeSession(sessionId: String)
}

fun List<ClientSessionData>.toUserList(): List<String> {
    return map { it.clientId }
}

fun Flow<List<ClientSessionData>>.toUserListFlow(): Flow<List<String>> {
    return map { it.toUserList() }
}
