package io.getsafenow.libraries.gsn_matrix.api.auth

import kotlinx.serialization.Serializable


@Serializable
data class OidcDetails(
    val url: String,
)
