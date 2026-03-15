package com.example.getsafenowclient.services

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class CallFirebaseMessagingService : FirebaseMessagingService() {

    // We might need to inject SessionManager/CallScreenModel if we want to trigger full sync
    // But for "App Killed" we mainly want to wake the Foreground Service UI.
    // The actual Sync can be kicked off by the Activity/Service startup logic or a WorkManager.

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // TODO: Update token on homeserver (Pusher set)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        val data = message.data
        if (data.isNotEmpty()) {
            handleDataMessage(data)
        }
    }

    private fun handleDataMessage(data: Map<String, String>) {
        // Standard Matrix Sygnal payload keys
        val eventId = data["event_id"]
        val roomId = data["room_id"]
        val type = data["type"]
        val senderName = data["sender_display_name"]
        
        // Element sometimes sends call invite as type="m.call.invite" or inside "content"
        // We look for indications of a call.
        val isCall = type == "m.call.invite" || data.containsKey("call_id")

        if (isCall) {
            // HIGH PRIORITY WAKE UP
            // Even if app is killed, we start the Foreground Service to show incoming UI.
            CallForegroundService.start(
                context = this,
                isIncoming = true,
                callerName = senderName ?: "Unknown Caller",
                callId = eventId
            )
        }
    }
}
