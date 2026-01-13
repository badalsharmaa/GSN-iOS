package io.getsafenow.services.gsnerror.api

import androidx.compose.runtime.Immutable

@Immutable
sealed interface AppErrorStateGsn {
    data object NoError : AppErrorStateGsn

    data class Error(
        val title: String,
        val body: String,
        val dismiss: () -> Unit,
    ) : AppErrorStateGsn
}