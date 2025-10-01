package io.getsafenow.libraries.gsn_theme.customtheme.themeutils

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.Foundation.NSProcessInfo
import platform.UIKit.UIApplication
import platform.UIKit.UIColor
import platform.UIKit.UISceneActivationStateForegroundActive
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene

// ---- Helpers ----
fun uiColorFromArgb(argb: Int): UIColor {
    val a = ((argb ushr 24) and 0xFF) / 255.0
    val r = ((argb ushr 16) and 0xFF) / 255.0
    val g = ((argb ushr 8) and 0xFF) / 255.0
    val b = (argb and 0xFF) / 255.0
    return UIColor.colorWithRed(r, green = g, blue = b, alpha = a)
}

fun keyWindow(): UIWindow? {
    // Prefer a foreground-active window scene
    val scenes = UIApplication.sharedApplication.connectedScenes
    val foreground = scenes.firstOrNull {
        (it as? UIWindowScene)?.activationState == UISceneActivationStateForegroundActive
    } as? UIWindowScene
        ?: scenes.firstOrNull { it is UIWindowScene } as? UIWindowScene

    // Use the scene's keyWindow when available; otherwise pick a visible window
    return foreground?.keyWindow
        ?: (foreground?.windows?.firstOrNull { (it as? UIWindow)?.hidden == false } as? UIWindow)
}

@OptIn(ExperimentalForeignApi::class)
fun isAtLeastIOS(major: Int): Boolean {
    val v = NSProcessInfo.processInfo.operatingSystemVersion
    return v.useContents {
        // majorVersion is NSInteger; .toInt() is safe
        majorVersion.toInt() >= major
    }
}
