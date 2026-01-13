package com.example.getsafenowclient.password.reset

sealed interface ResetPasswordEvent {
    data class CurrentPasswordChanged(val value: String) : ResetPasswordEvent
    data class NewPasswordChanged(val value: String) : ResetPasswordEvent
    data class ConfirmPasswordChanged(val value: String) : ResetPasswordEvent
    object Submit : ResetPasswordEvent
    object Cancel : ResetPasswordEvent
    object ErrorDismissed : ResetPasswordEvent
}
