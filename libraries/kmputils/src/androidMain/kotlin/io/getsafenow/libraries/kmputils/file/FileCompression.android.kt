package io.getsafenow.libraries.kmputils.file


/*
import co.touchlab.kermit.Logger
import io.getsafenow.libraries.kmputils.platformkmp.PlatformFile
import java.io.File

import java.util.zip.GZIPOutputStream

*/
/**
 * GZip a file.
 *
 * @param file the input file
 * @return the gzipped file
 *//*

actual fun compressFile(file: PlatformFile): PlatformFile? {
    Logger.v { "## compressFile() : compress ${file.path}" }

    val dstFile = PlatformFile(file.path + ".gz")

    if (dstFile.exists()) {
        dstFile.safeDelete()
    }

    return try {
        GZIPOutputStream(File(dstFile.path).outputStream()).use { gos ->
            java.io.File(file.path).inputStream().use { it.copyTo(gos, 2048) }
        }

        Logger.v { "## compressFile() : ${File(file.path).length()} compressed to ${java.io.File(dstFile.path).length()} bytes" }
        dstFile
    } catch (e: Exception) {
        Logger.e(message = { "## compressFile() failed" }, throwable = e)
        null
    } catch (oom: OutOfMemoryError) {
        Logger.e(message = { "## compressFile() failed" }, throwable = oom)
        null
    }
}*/
