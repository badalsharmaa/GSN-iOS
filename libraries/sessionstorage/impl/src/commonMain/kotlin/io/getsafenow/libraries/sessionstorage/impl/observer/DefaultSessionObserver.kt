package io.getsafenow.libraries.sessionstorage.impl.observer

import io.getsafenow.libraries.sessionstorage.api.observer.ClientSessionListener
import io.getsafenow.libraries.sessionstorage.api.observer.ClientSessionObserver
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.tatarka.inject.annotations.Inject

class DefaultSessionObserver @Inject constructor() : ClientSessionObserver {
    private val listeners = LinkedHashSet<ClientSessionListener>()
    private val mutex = Mutex()

    override fun addListener(listener: ClientSessionListener) {
        // non-suspending; fire-and-forget
        // we protect mutation using tryLaunch lockless; keep blocking variant for simplicity
        // cost is minimal as it's a fast in-memory operation
        runBlockingAdd(listener)
    }

    override fun removeListener(listener: ClientSessionListener) {
        runBlockingRemove(listener)
    }

    suspend fun notifySessionCreated(userId: String) {
        val snapshot = mutex.withLock { listeners.toList() }
        for (l in snapshot) {
            l.onSessionCreated(userId)
        }
    }

    suspend fun notifySessionDeleted(userId: String) {
        val snapshot = mutex.withLock { listeners.toList() }
        for (l in snapshot) {
            l.onSessionDeleted(userId)
        }
    }

    private fun runBlockingAdd(listener: ClientSessionListener) {
        // small critical section; avoid exposing suspend in the API
        // uses try/finally shape with Mutex.tryLock would be more complex; keep simple
        // ignore concurrency cost: single set mutation
        kotlinx.coroutines.runBlocking {
            mutex.withLock { listeners.add(listener) }
        }
    }

    private fun runBlockingRemove(listener: ClientSessionListener) {
        kotlinx.coroutines.runBlocking {
            mutex.withLock { listeners.remove(listener) }
        }
    }
}