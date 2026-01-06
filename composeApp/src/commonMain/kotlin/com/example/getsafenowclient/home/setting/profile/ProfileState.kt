package com.example.getsafenowclient.home.setting.profile

data class ProfileState(
    val username: String = "",
    val displayName: String = "",
    val email: String = "",
    val avatarUrl: String? = null,
    val isLoading: Boolean = false,
    val isResetPasswordDialogVisible: Boolean = false,
    val isSignOutDialogVisible: Boolean = false,
    val error: String? = null,

    // Edit States
    val isEditingDisplayName: Boolean = false,
    val tempDisplayName: String = "",

    val isEditingEmail: Boolean = false,
    val tempEmail: String = "",

    // Reset Password States
    val isResetPasswordLoading: Boolean = false,
    val resetPasswordError: String? = null
)
