package io.getsafenow.features.api


import kotlinx.serialization.Serializable

/**
 * Parameters to start the login flow, when the application is opened
 * from a mobile.element.io link.
 */
@Serializable
data class LoginParams(
    val accountProvider: String,
    val loginHint: String?
)
