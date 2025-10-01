package io.getsafenow.libraries.gsn_matrix.api.user

import io.getsafenow.libraries.di.SessionScopeGsn
import io.getsafenow.libraries.gsn_matrix.api.GsnMClient
import io.getsafenow.libraries.gsn_matrix.api.core.SessionId
import me.tatarka.inject.annotations.Inject

@SessionScopeGsn
class CurrentSessionIdHolder @Inject constructor(gsnMClient: GsnMClient) {
    val current = gsnMClient.sessionId

    fun isCurrentSession(sessionId: SessionId?): Boolean = current == sessionId
}
