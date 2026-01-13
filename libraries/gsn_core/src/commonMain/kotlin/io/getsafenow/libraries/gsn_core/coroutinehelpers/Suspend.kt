/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.getsafenow.libraries.gsn_core.coroutinehelpers

import kotlinx.coroutines.delay
import kotlin.time.measureTime

fun suspendWithMinimumDuration(
    minimumDurationMillis: Long = 500,
    block: suspend () -> Unit
) = suspend {
    val elapsed = measureTime {
        block()
    }
    val remaining = minimumDurationMillis - elapsed.inWholeMilliseconds
    if (remaining > 0) delay(remaining)
}
