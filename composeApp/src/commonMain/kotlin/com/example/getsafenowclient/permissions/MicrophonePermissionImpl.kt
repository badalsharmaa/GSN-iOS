package com.example.getsafenowclient.permissions

import io.getsafenow.libraries.kmputils.platformkmp.ContextFactory

expect class MicrophonePermissionImpl(contextFactory: ContextFactory) : MicrophonePermission {
    override fun hasPermission(): Boolean
    override suspend fun requestPermission(): Boolean
    override fun openSettings()
}
