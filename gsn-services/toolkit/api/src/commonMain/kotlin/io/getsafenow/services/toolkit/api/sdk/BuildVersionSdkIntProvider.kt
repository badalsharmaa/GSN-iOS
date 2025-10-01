package io.getsafenow.services.toolkit.api.sdk

/**
 * Platform-agnostic way to get the current OS "SDK version" (as an integer).
 */
interface BuildVersionSdkIntProvider {
    /**
     * Return the current version of the Android SDK.
     */
    fun get(): Int

    /**
     * Checks the if the current OS version is equal or greater than [version].
     * @return A `non-null` result if true, `null` otherwise.
     */
    fun <T> whenAtLeast(version: Int, result: () -> T): T? {
        return if (get() >= version) {
            result()
        } else {
            null
        }
    }

    fun isAtLeast(version: Int) = get() >= version
}