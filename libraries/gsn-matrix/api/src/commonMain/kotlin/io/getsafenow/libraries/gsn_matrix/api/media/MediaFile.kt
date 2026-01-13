package io.getsafenow.libraries.gsn_matrix.api.media

import io.getsafenow.libraries.kmputils.platformkmp.PlatformFile
import okio.Closeable


/**
 * A wrapper around a media file on the disk.
 * When closed the file will be removed from the disk unless [persist] has been used.
 */
interface MediaFile : Closeable {
    fun path(): String

    /**
     * Persists the temp file to the given path. The file will be moved to
     * the given path and won't be deleted anymore when closing the handle.
     */
    fun persist(path: String): Boolean
}

fun MediaFile.toFile(): PlatformFile {
    return PlatformFile(path())
}
