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
expect class PlatformFile(path: String) {
    /** Absolute or relative file system path. */
    val path: String
    /**
     * Returns true if this file exists on disk.
     */
    fun exists(): Boolean
    /**
     * Deletes this file if it exists.
     *
     * @return true if the file was successfully deleted, false otherwise.
     */
    fun delete(): Boolean
}
