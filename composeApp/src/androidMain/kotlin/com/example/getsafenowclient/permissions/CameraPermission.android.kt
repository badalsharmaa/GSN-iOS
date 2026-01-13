package com.example.getsafenowclient.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
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

// We reuse the global map from MicrophonePermission.android.kt if possible, 
// or create a new one. To avoid duplication or conflict if that map was private,
// we'll create a dedicated one for Camera here or rely on a shared PermissionManager if refactored.
// For now, following the pattern in MicrophonePermission.android.kt:

private val pendingCameraPermissions = mutableMapOf<String, CompletableDeferred<Boolean>>()

fun registerCameraPermissionHandler(activity: ComponentActivity) {
    val launcher = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        pendingCameraPermissions[Manifest.permission.CAMERA]?.complete(isGranted)
        pendingCameraPermissions.remove(Manifest.permission.CAMERA)
    }
    
    CameraPermissionActivityProvider.currentLauncher = launcher
    CameraPermissionActivityProvider.currentActivity = WeakReference(activity)
}

object CameraPermissionActivityProvider {
    var currentLauncher: androidx.activity.result.ActivityResultLauncher<String>? = null
    var currentActivity: WeakReference<ComponentActivity>? = null
}

actual class CameraPermissionImpl actual constructor(
    private val contextFactory: ContextFactory
) : CameraPermission {
    
    actual override fun hasPermission(): Boolean {
        val context = contextFactory.getApplication() as? Context ?: return false
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    actual override suspend fun requestPermission(): Boolean = withContext(Dispatchers.Main) {
        if (hasPermission()) return@withContext true
        
        val activity = CameraPermissionActivityProvider.currentActivity?.get()
        val launcher = CameraPermissionActivityProvider.currentLauncher

        if (activity == null || launcher == null) {
            Logger.e("CameraPermission: Activity or Launcher is null. Cannot request permission.")
            return@withContext false
        }

        val deferred = CompletableDeferred<Boolean>()
        pendingCameraPermissions[Manifest.permission.CAMERA] = deferred
        
        launcher.launch(Manifest.permission.CAMERA)
        
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
