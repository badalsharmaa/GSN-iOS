package io.getsafenow.libraries.kmputils.platformkmp

/**
 * Platform-neutral URI wrapper.
 * Works across Android (Uri) and iOS (NSURL).
 */
expect class PlatformUri {
    val raw: String

    companion object {
        fun parse(uri: String): PlatformUri?
    }

    fun toStringUri(): String
}

/**
 * A multiplatform abstraction over a file on disk.
 *
 * On Android → backed by [java.io.File].
 * On iOS     → backed by [NSFileManager] + [NSURL].
 *
 * Use this class instead of java.io.File in shared KMP code.
 */

/** Represents a file on disk in a platform-neutral way */
expect class PlatformFile(path: String) {

    /** Absolute or relative file system path */
    val path: String

    // Secondary constructor: parent directory + child path
    constructor(parent: PlatformFile, child: String)

    /** Checks if this file exists */
    fun exists(): Boolean

    /** Deletes this file */
    fun delete(): Boolean

    /** Creates directories if they don’t exist */
    fun mkdirs()

    /** Renames this file to another */
    fun renameTo(dest: PlatformFile): Boolean

    /** Returns length in bytes */
    fun length(): Long

    /** Walk through files recursively (top-down) */
    fun walkTopDown(): Sequence<PlatformFile>

    /** Deletes this file or directory recursively */
    fun deleteRecursively(): Boolean

    companion object {
        /** Creates a temporary file */
        fun createTempFile(prefix: String, suffix: String?, dir: PlatformFile): PlatformFile
    }
}


typealias ColorInt = Int

expect object ColorUtils {
    fun parse(colorString: String): ColorInt
}


/**
 * Provides access to platform-specific context and related file utilities.
 */
expect class ContextFactory {

    /** Returns a platform-specific context object (e.g., Android Context, iOS Bundle/UIApplication) */
    fun getContext(): Any

    /** Returns a platform-specific application object */
    fun getApplication(): Any

    /** Returns a platform-specific current activity / view controller object */
    fun getActivity(): Any

    // ---------------- Public utility functions ----------------

    /** Returns MIME type of a PlatformUri */
    fun getMimeType(uri: PlatformUri): String?

    /** Returns file name of a PlatformUri */
    fun getFileName(uri: PlatformUri): String?

    /** Returns file size of a PlatformUri */
    fun getFileSize(uri: PlatformUri): Long

    /** Returns default platform cache directory as a PlatformFile */
    fun cacheDir(): PlatformFile

    // ---------------- Internal helpers ----------------

    /**
     * Helper to get content file size for platform-specific URIs
     * Only used internally
     */
    internal fun getContentFileSize(uri: PlatformUri): Long?

    /**
     * Helper to get content file name for platform-specific URIs
     * Only used internally
     */
    internal fun getContentFileName(uri: PlatformUri): String?
}
