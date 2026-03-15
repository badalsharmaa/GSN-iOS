package com.example.getsafenowclient.notification

import com.example.getsafenowclient.call.SharedCallManager
import com.example.getsafenowclient.utils.LifecycleUtils
import io.getsafenow.libraries.kmputils.platformkmp.ContextFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import platform.Foundation.NSUUID
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter

import me.tatarka.inject.annotations.Inject

@Inject
actual class PlatformNotificationManager actual constructor(
    private val contextFactory: ContextFactory
) {

    // ✅ Use shared CallKit controller to avoid duplicate providers
    // NotificationDelegate no longer calls showIncomingCall for calls,
    // but keeping this implementation in case it's needed for edge cases
    actual fun showIncomingCall(
        callId: String,
        roomId: String,
        callerName: String,
        isVideo: Boolean
    ) {
        // Use shared CallKit controller
        val uuid = NSUUID()
        SharedCallManager.controller?.reportIncomingCall(uuid, callerName, isVideo)
    }

    actual fun showMessageNotification(
        roomId: String,
        senderName: String,
        messageBody: String,
        eventId: String
    ) {
        if (LifecycleUtils.isAppForeground.value) {
            // Show In-App Snackbar
            CoroutineScope(Dispatchers.Main).launch {
                GlobalSnackbarState.hostState.showSnackbar("$senderName: $messageBody")
            }
            return
        }

        val content = UNMutableNotificationContent().apply {
            setTitle(senderName)
            setBody(messageBody)
            setSound(platform.UserNotifications.UNNotificationSound.defaultSound)
            // setUserInfo(...) to pass room ID for click handling
        }

        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(0.1, repeats = false)
        val request = UNNotificationRequest.requestWithIdentifier(eventId, content, trigger)

        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request) { error ->
            if (error != null) {
                println("Failed to show local notification: $error")
            }
        }
    }
}
