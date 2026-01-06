package io.getsafenow.libraries.preferences.impl

import co.touchlab.stately.collections.ConcurrentMutableMap
import io.getsafenow.libraries.di.AppScopeGsn
import io.getsafenow.libraries.di.ApplicationContextGsn
import io.getsafenow.libraries.gsn_matrix.api.core.SessionId
import io.getsafenow.libraries.kmputils.platformkmp.ContextFactory
import io.getsafenow.libraries.preferences.api.SessionPreferencesStore
import io.getsafenow.libraries.preferences.api.SessionPreferencesStoreFactory
import io.getsafenow.libraries.sessionstorage.api.observer.ClientSessionListener
import io.getsafenow.libraries.sessionstorage.api.observer.ClientSessionObserver
import kotlinx.coroutines.CoroutineScope
import me.tatarka.inject.annotations.Inject
import software.amazon.lastmile.kotlin.inject.anvil.ContributesBinding
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn


@SingleIn(AppScopeGsn::class)
@ContributesBinding(AppScopeGsn::class)
class DefaultSessionPreferencesStoreFactory @Inject constructor(
    @ApplicationContextGsn private val context: ContextFactory,
    sessionObserver: ClientSessionObserver,
) : SessionPreferencesStoreFactory {
    private val cache = ConcurrentMutableMap<SessionId, DefaultSessionPreferencesStore>()

    init {
        sessionObserver.addListener(object : ClientSessionListener {
            override suspend fun onSessionCreated(userId: String) = Unit
            override suspend fun onSessionDeleted(userId: String) {
                val sessionPreferences = cache.remove(SessionId(userId))
                sessionPreferences?.clear()
            }
        })
    }

    override fun get(sessionId: SessionId, sessionCoroutineScope: CoroutineScope): SessionPreferencesStore = cache.getOrPut(sessionId) {
        DefaultSessionPreferencesStore(context, sessionId, sessionCoroutineScope)
    }

    override fun remove(sessionId: SessionId) {
        cache.remove(sessionId)
    }
}
