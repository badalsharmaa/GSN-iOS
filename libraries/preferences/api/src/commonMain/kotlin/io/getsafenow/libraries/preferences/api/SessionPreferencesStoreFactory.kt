package io.getsafenow.libraries.preferences.api

import io.getsafenow.libraries.gsn_matrix.api.core.SessionId
import kotlinx.coroutines.CoroutineScope


interface SessionPreferencesStoreFactory {
    fun get(sessionId: SessionId, sessionCoroutineScope: CoroutineScope): SessionPreferencesStore
    fun remove(sessionId: SessionId)
}
