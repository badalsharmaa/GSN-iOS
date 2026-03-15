package com.example.getsafenowclient.notification

import androidx.compose.material3.SnackbarHostState
import io.getsafenow.libraries.kmputils.platformkmp.ContextFactory
import me.tatarka.inject.annotations.Inject




// Shared state for Snacks
object GlobalSnackbarState {
    val hostState = SnackbarHostState()
}

expect class PlatformNotificationManager @Inject constructor(contextFactory: ContextFactory) {
    fun showIncomingCall(
        callId: String,
        roomId: String,
        callerName: String,
        isVideo: Boolean
    )
    
    fun showMessageNotification(
        roomId: String,
        senderName: String,
        messageBody: String,
        eventId: String
    )
}
