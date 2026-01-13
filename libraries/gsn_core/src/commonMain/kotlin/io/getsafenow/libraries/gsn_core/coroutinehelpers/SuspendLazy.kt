/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.getsafenow.libraries.gsn_core.coroutinehelpers

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Creates a lazily-started [Deferred] that runs [block] in the given [coroutineContext].
 *
 * The coroutine will only start when the [Lazy] is first accessed.
 */
fun <T> suspendLazy(coroutineContext: CoroutineContext = EmptyCoroutineContext, block: suspend () -> T): Lazy<Deferred<T>> {
    return lazy(LazyThreadSafetyMode.NONE) {
        val deferred = CompletableDeferred<T>()
        CoroutineScope(coroutineContext).launch {
            deferred.complete(block())
        }
        deferred
    }
}
