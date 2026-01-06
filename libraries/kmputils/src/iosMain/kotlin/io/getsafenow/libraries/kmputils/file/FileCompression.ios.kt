package io.getsafenow.libraries.kmputils.file

/*
import co.touchlab.kermit.Logger
import io.getsafenow.libraries.kmputils.platformkmp.PlatformFile
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfFile
import platform.zlib.Z_DEFAULT_COMPRESSION
import platform.zlib.Z_DEFLATED
import platform.zlib.Z_FINISH
import platform.zlib.Z_STREAM_END
import platform.zlib.deflate
import platform.zlib.deflateInit2

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
actual fun compressFile(file: PlatformFile): PlatformFile? {
    val dstPath = file.path + ".gz"
    val dstFile = PlatformFile(dstPath)

    if (dstFile.exists()) dstFile.safeDelete()

    return try {
        val srcData = NSData.dataWithContentsOfFile(file.path) ?: run {
            Logger.e { "compressFile: Failed to read file ${file.path}" }
            return null
        }

        val bytes = ByteArray(srcData.length.toInt())
        srcData.bytes?.let { ptr ->
            kotlinx.cinterop.memcpy(bytes.refTo(0), ptr, srcData.length)
        }

        val compressedBytes = bytes.gzip() ?: run {
            Logger.e { "compressFile: Failed to gzip file ${file.path}" }
            return null
        }

        NSData.create(bytes = compressedBytes.refTo(0), length = compressedBytes.size.toULong())!!.writeToFile(dstPath, atomically = true)
        Logger.v { "compressFile: ${bytes.size} bytes compressed to ${compressedBytes.size} bytes" }
        dstFile
    } catch (e: Throwable) {
        Logger.e(message = { "compressFile failed" }, throwable = e)
        null
    }
}
@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
private fun ByteArray.gzip(): ByteArray? = memScoped {
    val output = ByteArray(this@gzip.size + 1024)
    this@gzip.usePinned { inputPinned ->
        output.usePinned { outputPinned ->
            val strm = alloc<z_stream>().apply {
                zalloc = null
                zfree = null
                opaque = null
                next_in = inputPinned.addressOf(0)
                avail_in = this@gzip.size.convert()
                next_out = outputPinned.addressOf(0)
                avail_out = output.size.convert()
            }

            if (deflateInit2(strm.ptr, Z_DEFAULT_COMPRESSION, Z_DEFLATED, 31, 8, Z_DEFAULT_STRATEGY) != Z_OK) return null
            if (deflate(strm.ptr, Z_FINISH) != Z_STREAM_END) return null
            if (deflateEnd(strm.ptr) != Z_OK) return null

            output.copyOf(output.size - strm.avail_out.toInt())
        }
    }
}
*/
