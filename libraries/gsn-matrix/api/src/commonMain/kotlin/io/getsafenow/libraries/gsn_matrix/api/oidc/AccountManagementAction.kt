package io.getsafenow.libraries.gsn_matrix.api.oidc

import io.getsafenow.libraries.gsn_matrix.api.core.DeviceId

sealed interface AccountManagementAction {
    data object Profile : AccountManagementAction
    data object SessionsList : AccountManagementAction
    data class SessionView(val deviceId: DeviceId) : AccountManagementAction
    data class SessionEnd(val deviceId: DeviceId) : AccountManagementAction
}
