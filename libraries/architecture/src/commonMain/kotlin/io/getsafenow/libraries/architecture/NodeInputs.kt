/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.getsafenow.libraries.architecture

/**
 * Marker for typed inputs a screen/component can receive.
 * (Replacement for Appyx's Node Plugin pattern.)
 */
interface NodeInputs

/**
 * Implement on a screen/component that accepts inputs.
 */
interface InputsHolder {
    val nodeInputs: List<NodeInputs>
}

/**
 * Fetch a typed input from this component's inputs list.
 * Throws with a helpful message if not provided.
 */
inline fun <reified I : NodeInputs> InputsHolder.inputs(): I {
    return nodeInputs.filterIsInstance<I>().firstOrNull()
        ?: error("Missing input: ${I::class.simpleName}. Make sure to pass it in nodeInputs.")
}
