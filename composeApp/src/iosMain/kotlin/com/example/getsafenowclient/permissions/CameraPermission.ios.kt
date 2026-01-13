package com.example.getsafenowclient.permissions

import io.getsafenow.libraries.kmputils.platformkmp.ContextFactory
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual class CameraPermissionImpl actual constructor(
    private val contextFactory: ContextFactory
) : CameraPermission {

    @OptIn(ExperimentalForeignApi::class)
    actual override fun hasPermission(): Boolean {
        val status = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
        return status == AVAuthorizationStatusAuthorized
    }

    @OptIn(ExperimentalForeignApi::class)
    actual override suspend fun requestPermission(): Boolean {
        val status = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
        return when (status) {
            AVAuthorizationStatusAuthorized -> true
            AVAuthorizationStatusNotDetermined -> {
                suspendCoroutine { continuation ->
                    AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
                        continuation.resume(granted)
                    }
                }
            }
            else -> false // Denied or Restricted
        }
    }

    actual override fun openSettings() {
        val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
        if (UIApplication.sharedApplication.canOpenURL(url)) {
            UIApplication.sharedApplication.openURL(url)
        }
    }
}
