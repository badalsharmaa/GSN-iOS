/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.getsafenow.libraries.architecture


import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.subscribe

/**
 * Subscribes to lifecycle callbacks and logs them with the provided [log] function.
 * Works in KMP; pass Timber on Android or use the default println-based logger.
 */
fun Lifecycle.logLifecycle(
    name: String,
    tag: String = "Lifecycle",
    log: (String) -> Unit = { message -> println("$tag: $message") }
) {
    subscribe(
        onCreate  = { log("onCreate $name") },
        onStart   = { log("onStart $name") },
        onResume  = { log("onResume $name") },
        onPause   = { log("onPause $name") },
        onStop    = { log("onStop $name") },
        onDestroy = { log("onDestroy $name") },
    )
}
