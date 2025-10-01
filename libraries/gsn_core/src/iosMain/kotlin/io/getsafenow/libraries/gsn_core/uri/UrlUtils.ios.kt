package io.getsafenow.libraries.gsn_core.uri


import platform.Foundation.*

actual fun String.isValidUrl(): Boolean {
    val nsUrl = NSURL.URLWithString(this)
    return nsUrl != null && nsUrl.scheme != null && nsUrl.host != null
}
