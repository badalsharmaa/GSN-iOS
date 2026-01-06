package io.getsafenow.libraries.gsn_matrix.api.auth

sealed interface OidcPrompt {
    /**
     * The Authorization Server should prompt the End-User for
     * reauthentication.
     */
    data object Login : OidcPrompt

    /**
     * The Authorization Server should prompt the End-User to create a user
     * account.
     *
     * Defined in [Initiating User Registration via OpenID Connect](https://openid.net/specs/openid-connect-prompt-create-1_0.html).
     */
    data object Create : OidcPrompt

    /**
     * An unknown value.
     */
    data class Unknown(val value: String) : OidcPrompt
}
