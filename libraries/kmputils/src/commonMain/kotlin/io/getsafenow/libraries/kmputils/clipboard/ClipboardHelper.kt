package io.getsafenow.libraries.kmputils.clipboard

/**
 * Wrapper class for handling clipboard operations so it can be used in JVM environments.
 */
interface ClipboardHelper {
    fun copyPlainText(text: String)
}
