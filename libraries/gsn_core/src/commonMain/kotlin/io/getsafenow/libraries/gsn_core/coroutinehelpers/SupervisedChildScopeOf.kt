package io.getsafenow.libraries.gsn_core.coroutinehelpers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.job

/**
 * Creates a supervised child scope of the current scope.
 *
 * - Child scope is cancelled if the parent is cancelled.
 * - Exceptions in the parent cancel the child.
 * - Exceptions in the child do not cancel the parent.
 *
 * @param dispatcher Dispatcher to run coroutines on.
 * @param name Optional coroutine name (for debugging).
 */
fun CoroutineScope.supervisedChildScope(
    dispatcher: CoroutineDispatcher,
    name: String = "GsnChild",
): CoroutineScope {
    val supervisorJob = SupervisorJob(parent = coroutineContext.job)
    return CoroutineScope(coroutineContext + dispatcher + supervisorJob + CoroutineName(name))
}

