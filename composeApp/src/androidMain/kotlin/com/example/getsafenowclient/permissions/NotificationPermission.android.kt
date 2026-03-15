package com.example.getsafenowclient.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import co.touchlab.kermit.Logger
import io.getsafenow.libraries.kmputils.platformkmp.ContextFactory
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference

// Global map for notification permission requests
private val pendingNotificationPermissions = mutableMapOf<String, CompletableDeferred<Boolean>>()

// Hook for MainActivity to call - register notification permission handler
fun registerNotificationPermissionHandler(activity: ComponentActivity) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val launcher = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            pendingNotificationPermissions[Manifest.permission.POST_NOTIFICATIONS]?.complete(isGranted)
            pendingNotificationPermissions.remove(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        NotificationPermissionActivityProvider.currentLauncher = launcher
        NotificationPermissionActivityProvider.currentActivity = WeakReference(activity)
    }
}

object NotificationPermissionActivityProvider {
    var currentLauncher: androidx.activity.result.ActivityResultLauncher<String>? = null
    var currentActivity: WeakReference<ComponentActivity>? = null
}

actual class NotificationPermissionImpl actual constructor(
    private val contextFactory: ContextFactory
) : NotificationPermission {
    
    actual override fun hasPermission(): Boolean {
        // Android 12 and below: notifications are auto-granted
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true
        }
        
        val context = contextFactory.getApplication() as? Context ?: return false
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    actual override suspend fun requestPermission(): Boolean = withContext(Dispatchers.Main) {
        // Android 12 and below: auto-granted
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return@withContext true
        }
        
        if (hasPermission()) return@withContext true
        
        val activity = NotificationPermissionActivityProvider.currentActivity?.get()
        val launcher = NotificationPermissionActivityProvider.currentLauncher

        if (activity == null || launcher == null) {
            Logger.e("NotificationPermission: Activity or Launcher is null. Cannot request permission.")
            return@withContext false
        }

        val deferred = CompletableDeferred<Boolean>()
        pendingNotificationPermissions[Manifest.permission.POST_NOTIFICATIONS] = deferred
        
        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        
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
