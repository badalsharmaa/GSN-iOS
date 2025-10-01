package io.getsafenow.services.toolkit.api.systemclock

/**
 * Provides current epoch time in milliseconds.
 *
 * Keep this in common so modules can depend on it,
 * and implement it in the platform layer or in tests.
 */
fun interface SystemClock {
    fun epochMillis(): Long
}