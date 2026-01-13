package io.getsafenow.libraries.designcomponents.utils.snackbar

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import io.getsafenow.libraries.designcomponents.utils.SnackbarMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Mutex
import org.jetbrains.compose.resources.stringResource
import kotlin.concurrent.atomics.ExperimentalAtomicApi


/**
 * A global dispatcher of [io.getsafenow.libraries.designcomponents.utils.SnackbarMessage] to be displayed in [Snackbar] via a [SnackbarHostState].
 */
//class SnackbarDispatcher {
//    private val queueMutex = Mutex()
//    private val snackBarMessageQueue = ArrayDeque<SnackbarMessage>()
//    val snackbarMessage: Flow<SnackbarMessage?> = flow {
//        while (currentCoroutineContext().isActive) {
//            queueMutex.lock()
//            emit(snackBarMessageQueue.firstOrNull())
//        }
//    }
//
//    fun post(message: SnackbarMessage) {
//        if (snackBarMessageQueue.isEmpty()) {
//            snackBarMessageQueue.add(message)
//            if (queueMutex.isLocked) queueMutex.unlock()
//        } else {
//            snackBarMessageQueue.add(message)
//        }
//    }
//
//    fun clear() {
//        if (snackBarMessageQueue.isNotEmpty()) {
//            snackBarMessageQueue.removeFirstOrNull()
//            if (queueMutex.isLocked) queueMutex.unlock()
//        }
//    }
//}
//
///** Used to provide a [SnackbarDispatcher] to composable functions, it's needed for [rememberSnackbarHostState]. */
//val LocalSnackbarDispatcher = compositionLocalOf { SnackbarDispatcher() }
//
//@Composable
//fun SnackbarDispatcher.collectSnackbarMessageAsState(): State<SnackbarMessage?> {
//    return snackbarMessage.collectAsState(initial = null)
//}
//
///**
// * Helper method to display a [SnackbarMessage] in a [SnackbarHostState] handling cancellations.
// */
//@OptIn(ExperimentalAtomicApi::class)
//@Composable
//fun rememberSnackbarHostState(snackbarMessage: SnackbarMessage?): SnackbarHostState {
//    val snackbarHostState = remember { SnackbarHostState() }
//    val snackbarMessageText = snackbarMessage?.let {
//        stringResource(id = snackbarMessage.messageResId)
//    } ?: return snackbarHostState
//
//    val dispatcher = LocalSnackbarDispatcher.current
//    LaunchedEffect(snackbarMessage.id) {
//        // If the message wasn't already displayed, do it now, and mark it as displayed
//        // This will prevent the message from appearing in any other active SnackbarHosts
//        if (snackbarMessage.isDisplayed.getAndSet(true).not()) {
//            try {
//                snackbarHostState.showSnackbar(
//                    message = snackbarMessageText,
//                    duration = snackbarMessage.duration,
//                )
//                // The snackbar item was displayed and dismissed, clear its message
//                dispatcher.clear()
//            } catch (e: CancellationException) {
//                // The snackbar was being displayed when the coroutine was cancelled,
//                // so we need to clear its message
//                dispatcher.clear()
//                throw e
//            }
//        }
//    }
//    return snackbarHostState
//}