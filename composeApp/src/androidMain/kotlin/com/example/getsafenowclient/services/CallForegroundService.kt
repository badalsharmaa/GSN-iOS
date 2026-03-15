package com.example.getsafenowclient.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.getsafenowclient.MainActivity
import com.example.getsafenowclient.R

class CallForegroundService : Service() {

    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        val isIncoming = intent?.getBooleanExtra(EXTRA_IS_INCOMING, false) ?: false
        val callerName = intent?.getStringExtra(EXTRA_CALLER_NAME)
        val callId = intent?.getStringExtra(EXTRA_CALL_ID)
        val roomId = intent?.getStringExtra(EXTRA_ROOM_ID)  // ✅ Read roomId
        startForegroundService(isIncoming, callerName, callId, roomId)  // ✅ Pass roomId
        acquireWakeLock()
        
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseWakeLock()
    }

    private fun startForegroundService(isIncoming: Boolean, callerName: String?, callId: String?, roomId: String?) {
        // ✅ Use SINGLE_TOP to prevent app restart when clicking notification
        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            action = "com.example.getsafenowclient.INCOMING_CALL"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_IS_INCOMING, isIncoming)
            putExtra(EXTRA_CALLER_NAME, callerName)
            putExtra(EXTRA_CALL_ID, callId)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title = if (isIncoming) "Incoming Call" else "Call in Progress"
        val text = if (isIncoming) "${callerName ?: "Someone"} is calling..." else "Tap to return to call"
        
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setOngoing(true)
            .setAutoCancel(false)

        // ✅ Add Accept/Reject actions for incoming calls
        if (isIncoming && callId != null) {
            // Accept action
            val acceptIntent = Intent(this, com.example.getsafenowclient.notification.CallActionReceiver::class.java).apply {
                action = com.example.getsafenowclient.notification.CallActionReceiver.ACTION_ACCEPT_CALL
                putExtra(com.example.getsafenowclient.notification.CallActionReceiver.EXTRA_CALL_ID, callId)
                putExtra(com.example.getsafenowclient.notification.CallActionReceiver.EXTRA_ROOM_ID, roomId ?: "")  // ✅ Use actual roomId
            }
            val acceptPendingIntent = PendingIntent.getBroadcast(
                this,
                callId.hashCode() + 1,
                acceptIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            
            // Reject action
            val rejectIntent = Intent(this, com.example.getsafenowclient.notification.CallActionReceiver::class.java).apply {
                action = com.example.getsafenowclient.notification.CallActionReceiver.ACTION_REJECT_CALL
                putExtra(com.example.getsafenowclient.notification.CallActionReceiver.EXTRA_CALL_ID, callId)
            }
            val rejectPendingIntent = PendingIntent.getBroadcast(
                this,
                callId.hashCode() + 2,
                rejectIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            
            builder.addAction(R.drawable.ic_call, "Accept", acceptPendingIntent)
            builder.addAction(R.drawable.ic_call, "Reject", rejectPendingIntent)
            builder.setFullScreenIntent(pendingIntent, true)
        }

        val notification = builder.build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val type = if (Build.VERSION.SDK_INT >= 34) {
                 ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            } else {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            }
            startForeground(NOTIFICATION_ID, notification, type)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Call Service Channel",
                NotificationManager.IMPORTANCE_HIGH // HIGH for Heads-Up
            ).apply {
                description = "Notifications for active calls"
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setSound(null, null) // Application handles ringtone
                enableVibration(true)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    // ... WakeLocks ...
    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "GetSafeNowClient::CallServiceWakeLock"
            )
        }
        if (wakeLock?.isHeld == false) {
            wakeLock?.acquire(30 * 60 * 1000L /* 30 minutes */)
        }
    }

    private fun releaseWakeLock() {
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
    }

    companion object {
        const val CHANNEL_ID = "CallForegroundServiceChannel"
        const val NOTIFICATION_ID = 12345
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        
        const val EXTRA_IS_INCOMING = "EXTRA_IS_INCOMING"
        const val EXTRA_CALLER_NAME = "EXTRA_CALLER_NAME"
        const val EXTRA_CALL_ID = "EXTRA_CALL_ID"
        const val EXTRA_ROOM_ID = "EXTRA_ROOM_ID"  // ✅ Added

        fun start(context: Context, isIncoming: Boolean = false, callerName: String? = null, callId: String? = null, roomId: String? = null) {  // ✅ Added roomId parameter
            val intent = Intent(context, CallForegroundService::class.java)
            intent.action = ACTION_START
            intent.putExtra(EXTRA_IS_INCOMING, isIncoming)
            intent.putExtra(EXTRA_CALLER_NAME, callerName)
            intent.putExtra(EXTRA_CALL_ID, callId)
            intent.putExtra(EXTRA_ROOM_ID, roomId)  // ✅ Pass roomId
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        // ... stop ...
        fun stop(context: Context) {
            val intent = Intent(context, CallForegroundService::class.java)
            intent.action = ACTION_STOP
            context.startService(intent)
        }
    }
}
