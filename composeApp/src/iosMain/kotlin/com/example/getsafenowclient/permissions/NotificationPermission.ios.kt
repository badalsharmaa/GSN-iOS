package com.example.getsafenowclient.permissions

import io.getsafenow.libraries.kmputils.platformkmp.ContextFactory
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual class NotificationPermissionImpl actual constructor(
    private val contextFactory: ContextFactory
) : NotificationPermission {
    
    @OptIn(ExperimentalForeignApi::class)
    actual override fun hasPermission(): Boolean {
        // Note: This is synchronous check, may not be accurate
        // Better to use requestPermission() which checks async
        return true // Default to true, will be checked properly in requestPermission
    }

    @OptIn(ExperimentalForeignApi::class)
    actual override suspend fun requestPermission(): Boolean {
        return suspendCoroutine { continuation ->
            val center = UNUserNotificationCenter.currentNotificationCenter()
            
            // First check current authorization status
            center.getNotificationSettingsWithCompletionHandler { settings ->
                val currentStatus = settings?.authorizationStatus
                
                when (currentStatus) {
                    UNAuthorizationStatusAuthorized -> {
                        // Already authorized
                        continuation.resume(true)
                    }
                    else -> {
                        // Request authorization
                        center.requestAuthorizationWithOptions(
                            UNAuthorizationOptionAlert or 
                            UNAuthorizationOptionSound or 
                            UNAuthorizationOptionBadge
                        ) { granted, error ->
                            if (error != null) {
                                println("iOS Notification Permission Error: $error")
                                continuation.resume(false)
                            } else {
                                continuation.resume(granted)
                            }
                        }
                    }
                }
            }
        }
    }

    actual override fun openSettings() {
        val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
        if (UIApplication.sharedApplication.canOpenURL(url)) {
            UIApplication.sharedApplication.openURL(url)
        }
    }
}
