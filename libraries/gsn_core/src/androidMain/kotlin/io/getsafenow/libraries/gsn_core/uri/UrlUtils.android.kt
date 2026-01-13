package io.getsafenow.libraries.gsn_core.uri


import java.net.URI

actual fun String.isValidUrl(): Boolean {
    return try {
        URI(this).toURL()
        true
    } catch (t: Throwable) {
        false
    }
}
