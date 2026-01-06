package io.getsafenow.libraries.gsn_core.kmphash

import okio.ByteString
import okio.ByteString.Companion.encodeUtf8

/**
 * Policy:
 * - We will use SHA-256 only for identifiers.
 * - MD5 exists only as a helper for testing a 32-char (128-bit) SHA-256 variant for fixed-width keys.
 * - Fallback mirrors the original style: if hashing throws, we return hashCode().toString().
 *   (Note: hashCode() is not stable across processes/versions; use only as a last-resort fallback.)
 */

/** Full SHA-256 (lower-case hex) — preferred for identifiers. */
fun String.sha256Hex(): String =
    encodeUtf8().sha256().hex()

/** Full SHA-256 with fallback (matches original try/catch style). */
fun String.sha256HexOrFallback(): String = try {
    encodeUtf8().sha256().hex()
} catch (_: Throwable) {
    this.hashCode().toString()
}

/** 32-char (128-bit) SHA-256 truncated hex — for fixed-width cache/file keys. */
fun String.sha256Hex32(): String {
    val full = encodeUtf8().sha256().toByteArray() // 32 bytes
    val first16 = full.copyOf(16)                  // 128 bits
    return ByteString.of(*first16).hex()
}

/** 32-char (128-bit) SHA-256 truncated hex with fallback. */
fun String.sha256Hex32OrFallback(): String = try {
    val full = encodeUtf8().sha256().toByteArray()
    val first16 = full.copyOf(16)
    ByteString.of(*first16).hex()
} catch (_: Throwable) {
    this.hashCode().toString()
}

/** Debug/testing helper only: MD5 hex (lower-case). Not used in app logic or for security. */
@Deprecated(
    message = "Debug/testing helper only. Use sha256Hex() or sha256Hex32() instead."
)
fun String.md5(): String =
    encodeUtf8().md5().hex()

/** MD5 with fallback (for parity with the original style; debug/testing only). */
@Deprecated(
    message = "Debug/testing helper only. Use sha256HexOrFallback()/sha256Hex32OrFallback() instead."
)
fun String.md5OrFallback(): String = try {
    encodeUtf8().md5().hex()
} catch (_: Throwable) {
    this.hashCode().toString()
}