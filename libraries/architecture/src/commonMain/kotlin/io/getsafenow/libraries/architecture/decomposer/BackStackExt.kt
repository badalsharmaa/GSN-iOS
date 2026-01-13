package io.getsafenow.libraries.architecture.decomposer

import com.arkivanov.decompose.router.stack.ChildStack

/**
 * @return true if there's at least one screen behind the active one.
 */
fun <C : Any, T : Any> ChildStack<C, T>.canPop(): Boolean =
    backStack.isNotEmpty()
