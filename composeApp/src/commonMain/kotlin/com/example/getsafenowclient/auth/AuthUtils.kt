package com.example.getsafenowclient.auth

data class PasswordValidationState(
    val hasMinLength: Boolean = false,
    val hasUppercase: Boolean = false,
    val hasLowercase: Boolean = false,
    val hasNumber: Boolean = false,
    val hasSpecialChar: Boolean = false,
) {
    val isValid: Boolean
        get() = hasMinLength && hasUppercase && hasLowercase && hasNumber && hasSpecialChar
}

fun validatePassword(password: String): PasswordValidationState {
    val hasSpecialChar = password.any { char ->
        !char.isLetterOrDigit()
    }
    return PasswordValidationState(
        hasMinLength = password.length >= 8,
        hasUppercase = password.any { it.isUpperCase() },
        hasLowercase = password.any { it.isLowerCase() },
        hasNumber = password.any { it.isDigit() },
        hasSpecialChar = hasSpecialChar
    )
}