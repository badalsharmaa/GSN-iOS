package io.getsafenow.libraries.gsn_matrix.api.auth

import io.getsafenow.appconfig.AppConfigFactory

object OidcConfig {
    private val appConfig = AppConfigFactory.default

    val CLIENT_URI = appConfig.clientUri

    // Note: host must match with the host of CLIENT_URI
    val LOGO_URI = appConfig.logoUri

    // Note: host must match with the host of CLIENT_URI
    val TOS_URI = appConfig.tosUri

    // Note: host must match with the host of CLIENT_URI
    val POLICY_URI = appConfig.policyUri

    // Some homeservers/auth issuers don't support dynamic client registration, and have to be registered manually
    val STATIC_REGISTRATIONS = mapOf(
        //old reference
        "https://id.thirdroom.io/realms/thirdroom" to "elementx",
        "https://spydefense.org" to "here will be the server that we want our app preregister to first for an admin and public we will not use it."
    )
}
