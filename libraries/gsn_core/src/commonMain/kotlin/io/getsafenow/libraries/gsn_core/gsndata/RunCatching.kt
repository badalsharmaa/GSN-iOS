package io.getsafenow.libraries.gsn_core.gsndata

import kotlin.coroutines.cancellation.CancellationException

/**
 * Executes [operation] and returns its result, or `null` if an [Exception] is thrown.
 *
 * - If a [CancellationException] is thrown, it is rethrown.
 * - Optional [onException] callback can be used for logging or side effects.
 */
inline fun <A> runCatchingOrNull(
    onException: ((Exception) -> Unit) = { },
    operation: () -> A
): A? {
    return try {
        operation()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        onException.invoke(e)
        null
    }
}
