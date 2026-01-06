package io.getsafenow.libraries.preferences.impl

import io.getsafenow.libraries.di.SessionScopeGsn
import io.getsafenow.libraries.di.annotations.SessionCoroutineScopeGsn
import io.getsafenow.libraries.gsn_matrix.api.user.CurrentSessionIdHolder
import io.getsafenow.libraries.preferences.api.SessionPreferencesStore
import kotlinx.coroutines.CoroutineScope
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.ContributesTo

@Component
@ContributesTo(SessionScopeGsn::class)
object SessionPreferencesModule {
    @Provides
    fun providesSessionPreferencesStore(
        defaultSessionPreferencesStoreFactory: DefaultSessionPreferencesStoreFactory,
        currentSessionIdHolder: CurrentSessionIdHolder,
        @SessionCoroutineScopeGsn sessionCoroutineScope: CoroutineScope,
    ): SessionPreferencesStore {
        return defaultSessionPreferencesStoreFactory
            .get(currentSessionIdHolder.current, sessionCoroutineScope)
    }
}