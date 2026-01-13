package io.getsafenow.libraries.gsn_matrix.api.auth

interface OidcRedirectUrlProvider {
    fun provide(): String
}
