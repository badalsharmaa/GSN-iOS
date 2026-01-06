package io.getsafenow.libraries.gsn_matrix.api.user

import io.getsafenow.libraries.di.SessionScopeGsn
import io.getsafenow.libraries.gsn_matrix.api.GsnMClient
import io.getsafenow.libraries.gsn_matrix.api.core.SessionId
import me.tatarka.inject.annotations.Inject
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

@SingleIn(SessionScopeGsn::class)
class CurrentSessionIdHolder @Inject constructor(gsnMClient: GsnMClient) {
    val current = gsnMClient.sessionId

    fun isCurrentSession(sessionId: SessionId?): Boolean = current == sessionId
}
