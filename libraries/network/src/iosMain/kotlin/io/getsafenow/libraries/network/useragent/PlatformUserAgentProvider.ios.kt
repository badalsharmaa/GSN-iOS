package io.getsafenow.libraries.network.useragent

import platform.Foundation.NSBundle
import platform.UIKit.UIDevice

actual fun platformDeviceInfo(): String {
    val device = UIDevice.currentDevice
    return "${device.model} ${device.name}"
}

actual fun platformPlatformInfo(): String {
    val device = UIDevice.currentDevice
    return "${device.systemName} ${device.systemVersion}"
}

actual fun platformBuildInfo(): String {
    val bundle = NSBundle.mainBundle
    val buildNumber = bundle.objectForInfoDictionaryKey("CFBundleVersion") as? String ?: "Unknown"
    return "Build $buildNumber"
}