package io.getsafenow.libraries.gsn_matrix.api.auth

import kotlinx.serialization.Serializable


@Serializable
data class MHomeServerDetails(
    val url: String,
    val supportsPasswordLogin: Boolean,
    val supportsOidcLogin: Boolean,
)
