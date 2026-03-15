package com.example.getsafenowclient.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationManager
import co.touchlab.kermit.Logger
import com.example.getsafenowclient.MainActivity
import com.example.getsafenowclient.call.repository.CallStateRepository
import com.example.getsafenowclient.call.repository.IncomingCallData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver for handling call notification actions (Accept/Reject).
 * This allows users to accept/reject calls directly from the notification
 * without opening the app first.
 */
class CallActionReceiver : BroadcastReceiver() {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    override fun onReceive(context: Context, intent: Intent) {
        val callId = intent.getStringExtra(EXTRA_CALL_ID) ?: run {
            Logger.e { "CallActionReceiver: Missing call ID" }
            return
        }
        
        Logger.d { "CallActionReceiver: action=${intent.action} callId=$callId" }
        
        when (intent.action) {
            ACTION_ACCEPT_CALL -> handleAcceptCall(context, intent, callId)
            ACTION_REJECT_CALL -> handleRejectCall(context, callId)
            else -> Logger.w { "CallActionReceiver: Unknown action ${intent.action}" }
        }
    }
    
    private fun handleAcceptCall(context: Context, intent: Intent, callId: String) {
        val roomId = intent.getStringExtra(EXTRA_ROOM_ID) ?: run {
            Logger.e { "CallActionReceiver: Missing room ID for accept" }
            return
        }
        
        Logger.d { "CallActionReceiver: Accepting call roomId=$roomId callId=$callId" }
        
        // Open app to call screen with SINGLE_TOP to prevent restart
        val appIntent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_INCOMING_CALL
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_ROOM_ID, roomId)
            putExtra(EXTRA_CALL_ID, callId)
            putExtra(EXTRA_AUTO_ACCEPT, true) // Signal to auto-accept
        }
        
        context.startActivity(appIntent)
        
        // Dismiss notification
        dismissNotification(context, callId)
    }
    
    private fun handleRejectCall(context: Context, callId: String) {
        Logger.d { "CallActionReceiver: Rejecting call callId=$callId" }
        
        scope.launch {
            try {
                // TODO: Get CallStateRepository instance and clear state
                // For now, just dismiss notification
                // In full implementation, this should:
                // 1. Send reject event to Matrix
                // 2. Clear persisted call state
                // 3. Stop ringtone
                
                dismissNotification(context, callId)
                
            } catch (e: Exception) {
                Logger.e(e) { "Failed to reject call" }
            }
        }
    }
    
    private fun dismissNotification(context: Context, callId: String) {
        val notificationManager = context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as? NotificationManager
        
        notificationManager?.cancel(callId.hashCode())
        Logger.d { "CallActionReceiver: Dismissed notification for $callId" }
    }
    
    companion object {
        const val ACTION_ACCEPT_CALL = "com.example.getsafenowclient.ACCEPT_CALL"
        const val ACTION_REJECT_CALL = "com.example.getsafenowclient.REJECT_CALL"
        const val ACTION_INCOMING_CALL = "com.example.getsafenowclient.INCOMING_CALL"
        
        const val EXTRA_CALL_ID = "call_id"
        const val EXTRA_ROOM_ID = "room_id"
        const val EXTRA_AUTO_ACCEPT = "auto_accept"
    }
}
