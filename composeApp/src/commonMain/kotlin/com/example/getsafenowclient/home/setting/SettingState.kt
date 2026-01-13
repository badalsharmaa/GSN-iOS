package com.example.getsafenowclient.home.setting

data class SettingState(
    val userId: String = "",
    val userName: String = "",
    val userAvatarUrl: String? = null,
    val isLoading: Boolean = false
)
