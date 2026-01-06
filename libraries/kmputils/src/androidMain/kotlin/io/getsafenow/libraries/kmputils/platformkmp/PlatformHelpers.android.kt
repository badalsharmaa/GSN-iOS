package io.getsafenow.libraries.kmputils.platformkmp

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.graphics.toColorInt
import androidx.core.net.toFile
import androidx.core.net.toUri
import io.getsafenow.libraries.gsn_core.extensionshelper.runCatchingExceptions
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

    actual constructor(parent: PlatformFile, child: String) : this(File(parent.file, child).path)

    actual val path: String get() = file.path

    actual fun exists(): Boolean = file.exists()

    actual fun delete(): Boolean = file.delete()

    actual fun mkdirs() {
        if (!file.exists()) file.mkdirs()
    }

    actual fun renameTo(dest: PlatformFile): Boolean = file.renameTo(File(dest.path))

    actual fun length(): Long = file.length()

    actual fun walkTopDown(): Sequence<PlatformFile> =
        file.walkTopDown().map { PlatformFile(it.path) }

    actual fun deleteRecursively(): Boolean = file.deleteRecursively()

    actual companion object {
        actual fun createTempFile(prefix: String, suffix: String?, dir: PlatformFile): PlatformFile {
            val parentFile = File(dir.path).apply { if (!exists()) mkdirs() }
            val tempFile = File.createTempFile(prefix, suffix, parentFile)
            return PlatformFile(tempFile.path)
        }
    }
}


actual object ColorUtils {
    actual fun parse(colorString: String): ColorInt = colorString.toColorInt()
}

actual class ContextFactory(private val context: Context) {
    actual fun getContext(): Any = context
    actual fun getApplication(): Any = context.applicationContext
    actual fun getActivity(): Any = context
    actual fun cacheDir(): PlatformFile = PlatformFile(context.cacheDir.path)

    actual fun getMimeType(uri: PlatformUri): String? = runCatchingExceptions {
        val androidUri = uri.raw.toUri()
        when (androidUri.scheme) {
            ContentResolver.SCHEME_CONTENT -> getContextAs<Context>().contentResolver.getType(androidUri)
            else -> null
        }
    }.getOrNull()

    actual fun getFileName(uri: PlatformUri): String? = runCatchingExceptions {
        val androidUri = uri.raw.toUri()
        when (androidUri.scheme) {
            ContentResolver.SCHEME_CONTENT -> getContentFileName(uri) // pass PlatformUri
            ContentResolver.SCHEME_FILE -> androidUri.toFile().name
            else -> null
        }
    }.getOrNull()

    actual fun getFileSize(uri: PlatformUri): Long = runCatchingExceptions {
        val androidUri = uri.raw.toUri()
        when (androidUri.scheme) {
            ContentResolver.SCHEME_CONTENT -> getContentFileSize(uri) ?: 0L
            ContentResolver.SCHEME_FILE -> androidUri.toFile().length()
            else -> 0L
        }
    }.getOrNull() ?: 0L

// ---------- Internal helpers ----------

    internal actual fun getContentFileSize(uri: PlatformUri): Long? = runCatchingExceptions {
        val androidUri = uri.raw.toUri()// convert inside helper
        getContextAs<Context>().contentResolver.query(androidUri, null, null, null, null)?.use { cursor ->
            cursor.moveToFirst()
            cursor.getColumnIndexOrThrow(OpenableColumns.SIZE).let(cursor::getLong)
        }
    }.getOrNull()

    internal actual fun getContentFileName(uri: PlatformUri): String? = runCatchingExceptions {
        val androidUri = uri.raw.toUri() // convert inside helper
        getContextAs<Context>().contentResolver.query(androidUri, null, null, null, null)?.use { cursor ->
            cursor.moveToFirst()
            cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME).let(cursor::getString)
        }
    }.getOrNull()

    // ---------- Helper to cast context ----------
    private inline fun <reified T> ContextFactory.getContextAs(): T =
        getContext() as? T ?: throw IllegalStateException("ContextHelperAndroid - Context is not of type ${T::class}")

}