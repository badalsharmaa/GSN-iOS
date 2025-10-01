
package io.getsafenow.libraries.sessionstorage.api.observer

interface ClientSessionObserver {
    fun addListener(listener: ClientSessionListener)
    fun removeListener(listener: ClientSessionListener)
}
