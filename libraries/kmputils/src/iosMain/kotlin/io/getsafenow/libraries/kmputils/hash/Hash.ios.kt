package io.getsafenow.libraries.kmputils.hash

import co.touchlab.kermit.Logger
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.refTo
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CC_SHA512
import platform.CoreCrypto.CC_SHA512_DIGEST_LENGTH
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.dataUsingEncoding
import platform.Foundation.getBytes


@OptIn(ExperimentalForeignApi::class)
actual fun String.sha512Hash(): String {
    return try {
        val nsString = this as NSString
        val data: NSData = nsString.dataUsingEncoding(NSUTF8StringEncoding) ?: return hashCode().toString()

        val bytes = ByteArray(data.length.toInt())

        val digest = UByteArray(CC_SHA512_DIGEST_LENGTH.toInt())

        // Use pinned memory for both reading and hashing
        bytes.usePinned { pinned ->
            data.getBytes(pinned.addressOf(0))
            CC_SHA512(pinned.addressOf(0), bytes.size.toUInt(), digest.refTo(0))
        }

        digest.joinToString("") { it.toString(16).padStart(2, '0') }
    } catch (e: Throwable) {
        // Optional: log the exception
        Logger.e("sha512Hash failed ios: $e")
        hashCode().toString()
    }
}