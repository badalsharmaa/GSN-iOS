/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.getsafenow.libraries.architecture


import com.arkivanov.decompose.ComponentContext

/**
 * Factory that creates a Decompose component with an injected ComponentContext.
 * (Decompose ComponentContext; plugins are optional extras.)
 */

interface AssistedNodeFactory<NODE : Any> {
    fun create(
        componentContext: ComponentContext,
        // Optional extras kept for API continuity; pass DI helpers if you need.
        plugins: List<Any> = emptyList()
    ): NODE
}