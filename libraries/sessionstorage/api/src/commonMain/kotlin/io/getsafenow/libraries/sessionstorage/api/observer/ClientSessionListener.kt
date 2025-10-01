
package io.getsafenow.libraries.sessionstorage.api.observer

interface ClientSessionListener {
    suspend fun onSessionCreated(userId: String)
    suspend fun onSessionDeleted(userId: String)
}
