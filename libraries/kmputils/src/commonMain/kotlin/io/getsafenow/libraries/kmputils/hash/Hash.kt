package io.getsafenow.libraries.kmputils.hash

import co.touchlab.kermit.Logger

/**
 * Compute SHA-512 hash of a string in a platform-agnostic way.
 */
expect fun String.sha512Hash(): String

/**
 * Safe wrapper that logs any errors using Kermit
 */
fun String.safeSha512Hash(): String = try {
    sha512Hash()
} catch (e: Throwable) {
    Logger.e(message = { " Hash -> Failed to hash string: $this" }, throwable = e)
    hashCode().toString()
}