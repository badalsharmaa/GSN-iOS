package io.getsafenow.libraries.architecture

import androidx.compose.runtime.Composable

fun interface Presenter<State> {
    @Composable
    fun present(): State
}
