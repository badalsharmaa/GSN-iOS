package io.getsafenow.services.gsnerror.api

import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.StringResource

interface AppErrorStateServiceGsn {
    val appErrorStateFlow: StateFlow<AppErrorStateGsn>

    fun showError(title: String, body: String)

    fun showError(titleRes: StringResource,  bodyRes: StringResource)
}