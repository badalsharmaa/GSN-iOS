package com.example.getsafenowclient.auth

import com.example.getsafenowclient.component.OnboardingScreens


/**
 * Defines all possible user interactions and events on the authentication screen.
 */
sealed interface AuthEvent {
    data class ToggleScreen(val screen: OnboardingScreens) : AuthEvent
    data class UpdateUsername(val username: String) : AuthEvent
    data class UpdatePassword(val password: String) : AuthEvent
    data class UpdateEmail(val email: String) : AuthEvent
    data class ToggleTerms(val accepted: Boolean) : AuthEvent
    data class TogglePrivacy(val accepted: Boolean) : AuthEvent
    object LoginClicked : AuthEvent
    object SignUpClicked : AuthEvent

    object DismissEmailVerifyDialog : AuthEvent

    object CompleteEmailVerificationClicked : AuthEvent

}