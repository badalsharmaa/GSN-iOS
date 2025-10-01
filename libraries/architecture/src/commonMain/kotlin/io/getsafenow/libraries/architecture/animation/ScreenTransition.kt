package io.getsafenow.libraries.architecture.animation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.stack.animation.StackAnimation
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation

/**
 * Decompose rememberBackstackSlider(...)
 * Use it with Children(stack = ..., animation = rememberDefaultStackAnimation())
 */

@Composable
fun <C : Any, T : Any> rememberDefaultTransitionHandler(): StackAnimation<C, T> =
    stackAnimation(
        slide(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
    )

