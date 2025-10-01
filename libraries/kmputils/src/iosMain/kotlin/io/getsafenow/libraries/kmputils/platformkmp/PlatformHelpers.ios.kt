package io.getsafenow.libraries.kmputils.platformkmp


import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL

actual class PlatformUri internal constructor(private val nsUrl: NSURL) {
    actual val raw: String get() = nsUrl.absoluteString ?: ""

    actual fun toStringUri(): String = raw

    actual companion object {
        actual fun parse(uri: String): PlatformUri? {
            return NSURL(string = uri)?.let { PlatformUri(it) }
        }
    }
}

/**
 * iOS implementation of [PlatformFile].
 *
 * Uses Foundation [NSFileManager] and [NSURL].
 */
actual class PlatformFile actual constructor(path: String) {
    // Backing NSURL to interact with iOS file system
    private val nsUrl: NSURL = NSURL.fileURLWithPath(path)

    /** Normalized path string from NSURL. */
    actual val path: String = nsUrl.path ?: path

    /** Checks if the file exists using NSFileManager. */
    actual fun exists(): Boolean {
        val fileManager = NSFileManager.defaultManager
        return fileManager.fileExistsAtPath(path)
    }

    /** Attempts to delete the file using NSFileManager. */
    @OptIn(ExperimentalForeignApi::class)
    actual fun delete(): Boolean {
        val fileManager = NSFileManager.defaultManager
        return try {
            fileManager.removeItemAtPath(path, null)
            true
        } catch (e: Exception) {
            println("PlatformFile iOS -> $e")
            false
        }
    }
}
