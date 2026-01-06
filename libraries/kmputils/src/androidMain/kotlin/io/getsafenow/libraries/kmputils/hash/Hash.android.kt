package io.getsafenow.libraries.kmputils.hash

import co.touchlab.kermit.Logger
import java.security.MessageDigest
import java.util.Locale

actual fun String.sha512Hash(): String = try {
    val digest = MessageDigest.getInstance("SHA-512")
    digest.update(toByteArray())
    digest.digest()
        .joinToString("") { String.format(Locale.ROOT, "%02X", it) }
        .lowercase(Locale.ROOT)
} catch (exc: Exception) {
    // Fallback in case something goes wrong
    Logger.e("sha512Hash failed android: $exc")
    hashCode().toString()
}