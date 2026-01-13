package com.example.getsafenowclient.auth

import com.example.getsafenowclient.component.OnboardingScreens
import io.getsafenow.libraries.architecture.AsyncAction

/**
 * Represents the complete state of the authentication screen.
 */
data class AuthState(
    val selectedScreen: OnboardingScreens = OnboardingScreens.Login,
    val username: String = "",
    val password: String = "",
    val email: String = "",
    val passwordValidationState: PasswordValidationState = PasswordValidationState(),
    val termsAccepted: Boolean = false,
    val privacyAccepted: Boolean = false,
    val loginAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    val signUpAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    // NEW: UIA / email verification UI
    val isEmailVerifyDialogVisible: Boolean = false,
    val pendingVerifyEmail: String? = null,
    val pendingVerifyUsername: String? = null,
    val pendingVerifyPassword: String? = null,

    // Optional: a small action for the "I've verified" press
    val completeVerifyAction: AsyncAction<Unit> = AsyncAction.Uninitialized,

    // Inline error messages
    val emailError: String? = null,
    val usernameError: String? = null,
    val passwordError: String? = null,
    val genericError: String? = null,
)
