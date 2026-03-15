package com.example.getsafenowclient.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.getsafenowclient.MainActivity
import com.example.getsafenowclient.R
import com.example.getsafenowclient.services.CallForegroundService
import com.example.getsafenowclient.utils.LifecycleUtils
import io.getsafenow.libraries.kmputils.platformkmp.ContextFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import me.tatarka.inject.annotations.Inject

@Inject
actual class PlatformNotificationManager actual constructor(
    private val contextFactory: ContextFactory
) {
    private val context: Context get() = contextFactory.getContext() as Context

    init {
        createMessageChannel()
    }


    actual fun showIncomingCall(
        callId: String,
        roomId: String,
        callerName: String,
        isVideo: Boolean
    ) {
        CallForegroundService.start(
            context = context,
            isIncoming = true,
            callerName = callerName,
            callId = callId,
            roomId = roomId  // ✅ Pass roomId
        )
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

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Intent to open the chat (Simplified: opens Main -> Home)
        // In a real app we'd pass roomId to deep link
        val intent = Intent(context, MainActivity::class.java).apply {
             flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 
            roomId.hashCode(), 
            intent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Messaging Style is "Modern" for Android
        val person = androidx.core.app.Person.Builder()
            .setName(senderName)
            .build()

        val style = NotificationCompat.MessagingStyle(person)
            .addMessage(messageBody, System.currentTimeMillis(), person)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_MESSAGES)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setStyle(style)
            .setContentTitle(senderName) // Fallback
            .setContentText(messageBody) // Fallback
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setGroup(roomId) // Group by room

        notificationManager.notify(eventId.hashCode(), builder.build())
    }

    private fun createMessageChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_MESSAGES,
                "Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Chat messages"
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID_MESSAGES = "gsn_messages_channel"
    }
}
