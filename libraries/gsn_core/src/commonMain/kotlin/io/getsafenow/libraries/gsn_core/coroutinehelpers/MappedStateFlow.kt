/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.getsafenow.libraries.gsn_core.coroutinehelpers

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * A [StateFlow] whose value is derived from another [Flow].
 *
 * - Computes its value using [getValue].
 * - Collects from [sourceFlow] and exposes it as a [StateFlow].
 */
class MappedStateFlow<T>(
    private val getValue: () -> T,
    private val sourceFlow: Flow<T>
) : StateFlow<T> {

    override val replayCache: List<T>
        get() = listOf(value)

    override val value: T
        get() = getValue()

    override suspend fun collect(collector: FlowCollector<T>): Nothing {
        coroutineScope {
            sourceFlow
                .distinctUntilChanged()
                .stateIn(this)
                .collect(collector)
        }
    }
}

/**
 * Maps a [StateFlow] to another [StateFlow] by applying [transform].
 *
 * @param transform Function to transform the upstream value.
 */
fun <T, R> StateFlow<T>.mapState(transform: (T) -> R): StateFlow<R> =
    MappedStateFlow(
        getValue = { transform(value) },
        sourceFlow = map { transform(it) }
    )
