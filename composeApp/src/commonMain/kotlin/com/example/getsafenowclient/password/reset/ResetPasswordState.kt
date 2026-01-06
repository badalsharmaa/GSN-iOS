package com.example.getsafenowclient.password.reset

data class ResetPasswordState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)
