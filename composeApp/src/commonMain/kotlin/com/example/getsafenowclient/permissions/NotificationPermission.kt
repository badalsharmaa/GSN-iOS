package com.example.getsafenowclient.permissions

interface NotificationPermission {
    fun hasPermission(): Boolean
    suspend fun requestPermission(): Boolean
    fun openSettings()
}
