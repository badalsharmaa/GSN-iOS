package io.getsafenow.libraries.gsn_core.kmphash

import okio.ByteString.Companion.encodeUtf8

/**
 * Compute a Hash of a String, using md5 algorithm.
 */
fun String.md5(): String {
    return try {
        this.encodeUtf8().md5().hex()
    } catch (e: Exception) {
        // Fallback if md5 fails
        hashCode().toString()
    }
}
