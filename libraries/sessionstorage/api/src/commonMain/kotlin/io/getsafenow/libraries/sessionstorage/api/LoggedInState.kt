
package io.getsafenow.libraries.sessionstorage.api

sealed interface LoggedInState {
    data object NotLoggedIn : LoggedInState
    data class LoggedIn(
        val clientSessionId: String,
        val isTokenValid: Boolean,
    ) : LoggedInState
}
