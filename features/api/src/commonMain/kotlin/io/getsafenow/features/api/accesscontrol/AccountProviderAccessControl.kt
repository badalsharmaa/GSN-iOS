package io.getsafenow.features.api.accesscontrol

interface AccountProviderAccessControl {
    suspend fun isAllowedToConnectToAccountProvider(accountProviderUrl: String): Boolean
}
