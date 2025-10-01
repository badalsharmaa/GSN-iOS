package io.getsafenow.libraries.architecture

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.subscribe
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Return the most recent child instance whose configuration equals [navTarget], or null.
 */
fun <NavTarget : Any> BaseFlowNode<NavTarget>.childFor(navTarget: NavTarget): ScreenComponent? {
    val stackValue = stack.value
    return stackValue.items.lastOrNull { it.configuration == navTarget }?.instance
}

/**
 * Suspend until a child whose configuration matches [predicate] is ATTACHED
 * and return its instance typed as [N].
 */
suspend inline fun <reified N : ScreenComponent, NavTarget : Any>
        BaseFlowNode<NavTarget>.waitForChildAttached(
    crossinline predicate: (NavTarget) -> Boolean
): N = stack.waitForChildAttached(predicate)

/**
 * Suspend until a configuration matching [predicate] becomes ATTACHED.
 * (Does not return the instance; use when you only care that it's there.)
 */
suspend fun <NavTarget : Any>
        BaseFlowNode<NavTarget>.waitForNavTargetAttached(
    predicate: (NavTarget) -> Boolean
) = stack.waitForNavTargetAttached(predicate)

// ---- Helpers used by the public inline API above ----

@PublishedApi
internal suspend inline fun <reified N : ScreenComponent, NavTarget : Any>
        Value<ChildStack<NavTarget, ScreenComponent>>.waitForChildAttached(
    crossinline predicate: (NavTarget) -> Boolean
): N = suspendCancellableCoroutine { cont ->
    fun tryResume(stack: ChildStack<NavTarget, ScreenComponent>) {
        val match = stack.items.lastOrNull { predicate(it.configuration) }?.instance as? N
        if (match != null && !cont.isCompleted) cont.resume(match)
    }
    tryResume(value)
    val disposable = subscribe { stack -> tryResume(stack) }
    cont.invokeOnCancellation { disposable.cancel() }
}

@PublishedApi
internal suspend fun <NavTarget : Any>
        Value<ChildStack<NavTarget, ScreenComponent>>.waitForNavTargetAttached(
    predicate: (NavTarget) -> Boolean
) = suspendCancellableCoroutine { cont ->
    fun tryResume(stack: ChildStack<NavTarget, ScreenComponent>) {
        val present = stack.items.any { predicate(it.configuration) }
        if (present && !cont.isCompleted) cont.resume(Unit)
    }
    tryResume(value)
    val disposable = subscribe { stack -> tryResume(stack) }
    cont.invokeOnCancellation { disposable.cancel() }
}

