package io.getsafenow.appdi.componentdi

import io.getsafenow.libraries.di.SessionScopeGsn
import io.getsafenow.libraries.gsn_matrix.api.GsnMClient
import io.getsafenow.libraries.gsn_matrix.api.user.CurrentSessionIdHolder
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@SessionScopeGsn
@Component
abstract class SessionComponentGsn(
    // Bound instance for this session
    @get:Provides val client: GsnMClient,
) {
    abstract val currentSessionIdHolder: CurrentSessionIdHolder
    // expose other session-scoped deps here…
}