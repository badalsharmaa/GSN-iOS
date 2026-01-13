package com.example.getsafenowclient.permissions

interface MicrophonePermission {
    fun hasPermission(): Boolean
    suspend fun requestPermission(): Boolean
    fun openSettings()
}
