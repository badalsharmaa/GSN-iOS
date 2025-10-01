package io.getsafenow.libraries.gsn_core.coroutinehelpers

import kotlinx.coroutines.flow.flow


/**
 * Creates a [Flow] that immediately throws [throwable] when collected.
 *
 * Useful for testing error handling in flows.
 */
fun <T> flowError(throwable: Throwable) = flow<T> { throw throwable }
