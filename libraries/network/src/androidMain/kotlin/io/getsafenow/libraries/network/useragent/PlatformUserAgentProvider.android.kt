package io.getsafenow.libraries.network.useragent

import android.os.Build

actual fun platformDeviceInfo(): String {
    return "${Build.MANUFACTURER} ${Build.MODEL}"
}

actual fun platformPlatformInfo(): String {
    return "Android ${Build.VERSION.RELEASE}"
}

actual fun platformBuildInfo(): String {
    return "Build ${Build.DISPLAY}"
}