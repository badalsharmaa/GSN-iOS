package io.getsafenow.libraries.gsn_matrix.impl.auth

import io.getsafenow.libraries.gsn_matrix.api.auth.MHomeServerDetails
import net.folivo.trixnity.clientserverapi.client.MatrixClientServerApiClient


import io.ktor.http.Url
import net.folivo.trixnity.clientserverapi.client.MatrixClientServerApiClientImpl
import net.folivo.trixnity.clientserverapi.model.authentication.LoginType

/**
 * Fetches Matrix homeserver details (login flow support, etc.)
 * Trixnity KMP version equivalent of RustSDK's HomeserverLoginDetails.map()
 */
suspend fun fetchHomeServerDetails(baseUrl: String): MHomeServerDetails {
    val apiClient = MatrixClientServerApiClientImpl(baseUrl = Url(baseUrl))
    return try {
        val loginTypesResult = apiClient.authentication.getLoginTypes()
        val types = loginTypesResult.getOrNull() ?: emptySet()

        // ✅ Correct mapping for your LoginType model
        val supportsPassword = types.any { it is LoginType.Password }
        val supportsSso = types.any { it is LoginType.SSO }

        MHomeServerDetails(
            url = baseUrl,
            supportsPasswordLogin = supportsPassword,
            supportsOidcLogin = false
        )
    } finally {
        apiClient.closeSuspending()
    }
}
