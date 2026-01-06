package com.example.getsafenowclient.permissions

interface CameraPermission {
    fun hasPermission(): Boolean
    suspend fun requestPermission(): Boolean
    fun openSettings()
}
