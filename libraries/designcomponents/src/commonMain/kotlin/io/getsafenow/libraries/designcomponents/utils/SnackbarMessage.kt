package io.getsafenow.libraries.designcomponents.utils

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarDuration.Short
import org.jetbrains.compose.resources.StringResource
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.random.Random

/**
 * A message to be displayed in a [Snackbar].
 * @param messageResId The message to be displayed.
 * @param duration The duration of the message. The default value is [SnackbarDuration.Short].
 * @param actionResId The action text to be displayed. The default value is `null`.
 * @param isDisplayed Used to track if the current message is already displayed or not.
 * @param id The unique identifier of the message. The default value is a random long.
 * @param action The action to be performed when the action is clicked.
 */

data class SnackbarMessage @OptIn(ExperimentalAtomicApi::class) constructor(
    val messageResId: StringResource,
    val duration: SnackbarDuration = SnackbarDuration.Short,
    val actionResId: StringResource? = null,
    val isDisplayed: AtomicBoolean = AtomicBoolean(false),
    val id: Long = Random.nextLong(),
    val action: () -> Unit = {},
)
