package com.example.getsafenowclient.permissions

import io.getsafenow.libraries.kmputils.platformkmp.ContextFactory

expect class CameraPermissionImpl(contextFactory: ContextFactory) : CameraPermission {
    override fun hasPermission(): Boolean
    override suspend fun requestPermission(): Boolean
    override fun openSettings()
}
