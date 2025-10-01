package io.getsafenow.libraries.gsn_core.coroutinehelpers

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.runningFold

/**
 * Returns the first element of the flow that is an instance of [T], waiting for it if necessary.
 */
suspend inline fun <reified T> Flow<*>.firstInstanceOf(): T {
    return first { it is T } as T
}

/**
 * Returns a flow that emits pairs of the previous and current values.
 * The first emission will be a pair of `null` and the first value emitted by the source flow.
 */
fun <T> Flow<T>.withPreviousValue(): Flow<Pair<T?, T>> {
    return runningFold(null) { prev: Pair<T?, T>?, current ->
        prev?.second to current
    }
        .filterNotNull()
}
