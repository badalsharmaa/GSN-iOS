package com.example.getsafenowclient.password.reset

import androidx.compose.runtime.Composable
import co.touchlab.kermit.Logger
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import io.getsafenow.libraries.architecture.ScreenComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.clientserverapi.client.UIA
import net.folivo.trixnity.clientserverapi.model.authentication.IdentifierType
import net.folivo.trixnity.clientserverapi.model.uia.AuthenticationRequest

interface ResetPasswordComponent : ScreenComponent {
    fun submitResetPassword(current: String, new: String)
}

class ResetPasswordScreenModel(
    componentContext: ComponentContext,
    private val client: MatrixClient,
    private val onSuccess: () -> Unit
) : ResetPasswordComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate).also { sc ->
        lifecycle.doOnDestroy { sc.cancel() }
    }

    override fun submitResetPassword(current: String, new: String) {
        scope.launch {
            try {
                val result = client.api.authentication.changePassword(
                    newPassword = new
                ).getOrThrow()

                when (result) {
                    is UIA.Success -> onSuccess()
                    is UIA.Step -> {
                        // The server requires additional authentication (UIA).
                        // We attempt to satisfy the password stage using the provided current password.
                        val authResponse = result.authenticate(
                            AuthenticationRequest.Password(
                                identifier = IdentifierType.User(client.userId.full),
                                password = current
                            )
                        ).getOrThrow()

                        if (authResponse is UIA.Success) {
                            onSuccess()
                        } else {
                            // Handle UIA failure (e.g. wrong password) or further steps
                            Logger.withTag("ResetPasswordScreenModel").e("Reset password UIA failed or requires more steps: $authResponse")
                            // In a real app, update state to show error to user
                        }
                    }
                    is UIA.Error -> {
                        Logger.withTag("ResetPasswordScreenModel").e("Reset password UIA Error: ${result.errorResponse}")
                    }
                }
            } catch (e: Exception) {
                Logger.withTag("ResetPasswordScreenModel").e("Error resetting password: $e")
            }
        }
    }

    @Composable
    override fun Render() {}
}
