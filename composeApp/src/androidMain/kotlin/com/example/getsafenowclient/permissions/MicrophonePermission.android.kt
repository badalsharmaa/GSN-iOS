package com.example.getsafenowclient.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import co.touchlab.kermit.Logger
import io.getsafenow.libraries.kmputils.platformkmp.ContextFactory
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference

// Global map to hold pending permission requests.
// Key: Permission Name, Value: Deferred result
private val pendingPermissions = mutableMapOf<String, CompletableDeferred<Boolean>>()

// Hook for MainActivity to call
fun registerPermissionHandler(activity: ComponentActivity) {
    val launcher = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        pendingPermissions[Manifest.permission.RECORD_AUDIO]?.complete(isGranted)
        pendingPermissions.remove(Manifest.permission.RECORD_AUDIO)
    }
    
    PermissionActivityProvider.currentLauncher = launcher
    PermissionActivityProvider.currentActivity = WeakReference(activity)
}

object PermissionActivityProvider {
    var currentLauncher: androidx.activity.result.ActivityResultLauncher<String>? = null
    var currentActivity: WeakReference<ComponentActivity>? = null
}

actual class MicrophonePermissionImpl actual constructor(
    private val contextFactory: ContextFactory
) : MicrophonePermission {
    
    actual override fun hasPermission(): Boolean {
        val context = contextFactory.getApplication() as? Context ?: return false
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    actual override suspend fun requestPermission(): Boolean = withContext(Dispatchers.Main) {
        if (hasPermission()) return@withContext true
        
        val activity = PermissionActivityProvider.currentActivity?.get()
        val launcher = PermissionActivityProvider.currentLauncher

        if (activity == null || launcher == null) {
            Logger.e("MicrophonePermission: Activity or Launcher is null. Cannot request permission.")
            return@withContext false
        }

        val deferred = CompletableDeferred<Boolean>()
        pendingPermissions[Manifest.permission.RECORD_AUDIO] = deferred
        
        launcher.launch(Manifest.permission.RECORD_AUDIO)
        
        deferred.await()
    }

    actual override fun openSettings() {
        val context = contextFactory.getApplication() as? Context ?: return
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
