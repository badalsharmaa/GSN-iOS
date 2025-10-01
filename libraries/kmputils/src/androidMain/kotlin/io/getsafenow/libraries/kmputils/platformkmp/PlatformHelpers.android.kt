package io.getsafenow.libraries.kmputils.platformkmp

import android.net.Uri
import androidx.core.net.toUri
import java.io.File

actual class PlatformUri internal constructor(private val uri: Uri) {
    actual val raw: String get() = uri.toString()

    actual fun toStringUri(): String = raw
    actual companion object {
        actual fun parse(uri: String): PlatformUri? {
            return try {
                PlatformUri(uri.toUri())
            } catch (e: Exception) {
                null
            }
        }
    }
}

/**
 * Android implementation of [PlatformFile].
 *
 * Uses the standard [java.io.File] API.
 */
actual class PlatformFile actual constructor(path: String) {
    private val file = File(path)

    /** Path string from the underlying File. */
    actual val path: String get() = file.path

    /** Checks if the file exists. */
    actual fun exists(): Boolean = file.exists()

    /** Attempts to delete the file. */
    actual fun delete(): Boolean = file.delete()
}