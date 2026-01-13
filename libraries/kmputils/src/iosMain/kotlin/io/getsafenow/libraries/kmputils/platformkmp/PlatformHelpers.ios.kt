package io.getsafenow.libraries.kmputils.platformkmp


import co.touchlab.kermit.Logger
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSBundle
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.NSUserDomainMask
import platform.UIKit.UIApplication

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
    private val nsUrl = NSURL.fileURLWithPath(path)

    actual val path: String = nsUrl.path ?: path

    // Secondary constructor
    actual constructor(parent: PlatformFile, child: String) : this(
        if (parent.path.endsWith("/")) "${parent.path}$child"
        else "${parent.path}/$child"
    )

    actual fun exists(): Boolean = NSFileManager.defaultManager.fileExistsAtPath(path)

    @OptIn(ExperimentalForeignApi::class)
    actual fun delete(): Boolean = try {
        NSFileManager.defaultManager.removeItemAtPath(path, null)
        true
    } catch (e: Throwable) {
        false
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun mkdirs() {
        try {
            NSFileManager.defaultManager.createDirectoryAtPath(path, true, null, null)
        } catch (_: Throwable) { }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun renameTo(dest: PlatformFile): Boolean = try {
        NSFileManager.defaultManager.moveItemAtPath(path, dest.path, null)
        true
    } catch (_: Throwable) { false }

    @OptIn(ExperimentalForeignApi::class)
    actual fun length(): Long {
        return try {
            val attrs = NSFileManager.defaultManager.attributesOfItemAtPath(path, null)
            (attrs?.get("NSFileSize") as? Long) ?: 0L
        } catch (_: Throwable) { 0L }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun walkTopDown(): Sequence<PlatformFile> {
        val files = mutableListOf<PlatformFile>()
        val fm = NSFileManager.defaultManager
        val enumerator = fm.enumeratorAtPath(path)
        while (enumerator?.nextObject() != null) {
            val filePath = "$path/${enumerator.nextObject()}"
            files.add(PlatformFile(filePath))
        }
        return files.asSequence()
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun deleteRecursively(): Boolean = try {
        NSFileManager.defaultManager.removeItemAtPath(path, null)
        true
    } catch (e: Throwable) {
        Logger.e("${e.message} Platform File Ios deleteRecursively failed.")
        false
    }
    actual companion object {
        actual fun createTempFile(prefix: String, suffix: String?, dir: PlatformFile): PlatformFile {
            val tmpName = (prefix + NSUUID().UUIDString + (suffix ?: ""))
            val tmpPath = "${dir.path}/$tmpName"
            return PlatformFile(tmpPath)
        }
    }
}

actual object ColorUtils {
    actual fun parse(colorString: String): ColorInt {
        // crude parser for #RRGGBB / #AARRGGBB
        val hex = colorString.removePrefix("#")
        val intValue = hex.toLong(16)
        return when (hex.length) {
            6 -> (0xFF shl 24 or intValue.toInt()) // add alpha
            8 -> intValue.toInt()
            else -> throw IllegalArgumentException("Unsupported color: $colorString")
        }
    }
}

actual class ContextFactory {

    // ---------------- Platform context access ----------------

    actual fun getContext(): Any = NSBundle.mainBundle
    actual fun getApplication(): Any = UIApplication.sharedApplication
    actual fun getActivity(): Any =
        UIApplication.sharedApplication.keyWindow?.rootViewController ?: UIApplication.sharedApplication

    // ---------------- Public functions ----------------

    actual fun cacheDir(): PlatformFile {
        val dir = (NSSearchPathForDirectoriesInDomains(
            directory = NSCachesDirectory,
            domainMask = NSUserDomainMask,
            expandTilde = true
        ).firstOrNull() as? String) ?: NSTemporaryDirectory()
        return PlatformFile(dir)
    }

    actual fun getMimeType(uri: PlatformUri): String? {
        val path = uri.toStringUri()
        val ext = path.substringAfterLast(".", "")
        val mime = when (ext.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "txt" -> "text/plain"
            "pdf" -> "application/pdf"
            else -> null
        }
        Logger.v { "getMimeType: $path -> $mime" }
        return mime
    }

    actual fun getFileName(uri: PlatformUri): String? {
        val file = PlatformFile(uri.toStringUri())
        val name = file.path.substringAfterLast("/")
        Logger.v { "getFileName: ${file.path} -> $name" }
        return name
    }

    actual fun getFileSize(uri: PlatformUri): Long {
        val file = PlatformFile(uri.toStringUri())
        val size = if (file.exists()) getContentFileSize(uri) ?: 0 else 0
        Logger.v { "getFileSize: ${file.path} -> $size" }
        return size
    }

    // ---------------- Internal helpers ----------------

    @OptIn(ExperimentalForeignApi::class)
    internal actual fun getContentFileSize(uri: PlatformUri): Long? {
        val file = PlatformFile(uri.toStringUri())
        return try {
            if (file.exists()) {
                val attrs = NSFileManager.defaultManager.attributesOfItemAtPath(file.path, null)
                val size = attrs?.get("NSFileSize") as? Long
                Logger.v { "getContentFileSize: ${file.path} -> $size" }
                size
            } else null
        } catch (e: Exception) {
            Logger.e(message = { "Error getting file size for ${file.path}" }, throwable = e)
            null
        }
    }

    internal actual fun getContentFileName(uri: PlatformUri): String? {
        val file = PlatformFile(uri.toStringUri())
        val name = if (file.exists()) file.path.substringAfterLast("/") else null
        Logger.v { "getContentFileName: ${file.path} -> $name" }
        return name
    }
}