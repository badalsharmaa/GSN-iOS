package io.getsafenow.libraries.kmputils.metadata

import io.getsafenow.libraries.kmputils.BuildKonfig

/**
 * true if the app is built in debug mode.
 * For testing purpose, this can be changed with [withReleaseBehavior].
 */
var isInDebug: Boolean = BuildKonfig.DEBUG
    private set

/**
 * Run the lambda simulating the app is in release mode.
 *
 * ONLY use for testing purposes.
 */
fun withReleaseBehavior(lambda: () -> Unit) {
    val previous = isInDebug
    try {
        isInDebug = false
        lambda()
    } finally {
        isInDebug = BuildKonfig.DEBUG
    }
}
