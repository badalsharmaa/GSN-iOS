package com.example.getsafenowclient

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.getsafenowclient.permissions.registerCameraPermissionHandler
import com.example.getsafenowclient.permissions.registerPermissionHandler
import com.example.getsafenowclient.permissions.registerNotificationPermissionHandler
import io.getsafenow.libraries.kmputils.platformkmp.ContextFactory


class AndroidApp : Application() {
    companion object {
        lateinit var INSTANCE: AndroidApp
    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val contextFactory = ContextFactory(this)
        
        // Hook for our custom permission handlers
        registerPermissionHandler(this)       // For Microphone
        registerCameraPermissionHandler(this) // For Camera
        registerNotificationPermissionHandler(this)  // ✅ For Notifications (Android 13+)

        // Process initial intent
        val extras = processCallIntent(intent)

        setContent {
            App(
                ContextFactory(this),
                extras
            )
        }
    }
    
    /**
     * Handle new intents when app is already running.
     * This prevents app restart when clicking notification.
     */
    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        
        // Process the new intent
        val extras = processCallIntent(intent)
        
        // TODO: Notify the app about the new intent
        // This could be done via a shared flow or event bus
        co.touchlab.kermit.Logger.d { 
            "MainActivity: onNewIntent action=${intent.action} extras=$extras" 
        }
    }
    
    /**
     * Process call-related intent and extract extras.
     */
    private fun processCallIntent(intent: android.content.Intent?): Map<String, Any?>? {
        if (intent == null) return null
        
        val action = intent.action
        co.touchlab.kermit.Logger.d { "MainActivity: Processing intent action=$action" }
        
        return when (action) {
            "com.example.getsafenowclient.INCOMING_CALL" -> {
                intent.extras?.let { bundle ->
                    mapOf(
                        "EXTRA_IS_INCOMING" to bundle.getBoolean("EXTRA_IS_INCOMING"),
                        "EXTRA_CALLER_NAME" to bundle.getString("EXTRA_CALLER_NAME"),
                        "EXTRA_CALL_ID" to bundle.getString("EXTRA_CALL_ID"),
                        "EXTRA_AUTO_ACCEPT" to bundle.getBoolean("EXTRA_AUTO_ACCEPT", false)
                    )
                }
            }
            else -> {
                intent.extras?.let { bundle ->
                    mapOf(
                        "EXTRA_IS_INCOMING" to bundle.getBoolean("EXTRA_IS_INCOMING"),
                        "EXTRA_CALLER_NAME" to bundle.getString("EXTRA_CALLER_NAME"),
                        "EXTRA_CALL_ID" to bundle.getString("EXTRA_CALL_ID")
                    )
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        com.example.getsafenowclient.utils.LifecycleUtils.onAppForeground()
    }

    override fun onPause() {
        super.onPause()
        com.example.getsafenowclient.utils.LifecycleUtils.onAppBackground()
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    val ctx = LocalContext.current
    App(contextFactory = ContextFactory(ctx))
}
