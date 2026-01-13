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

        // Use the generated builder
       /* val appComponent = AppComponentGsn.create(contextFactory)*/
        setContent {
            App(
                ContextFactory(this)
            )
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    val ctx = LocalContext.current
    App(contextFactory = ContextFactory(ctx))
}
