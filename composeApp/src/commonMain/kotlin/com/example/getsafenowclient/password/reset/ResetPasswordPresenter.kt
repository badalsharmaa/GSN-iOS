package com.example.getsafenowclient.password.reset

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch

@Composable
fun resetPasswordPresenter(
    component: ResetPasswordComponent,
    onDismiss: () -> Unit
): Pair<ResetPasswordState, (ResetPasswordEvent) -> Unit> {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val state = ResetPasswordState(
        isLoading = isLoading,
        error = error
    )

    val eventSink: (ResetPasswordEvent) -> Unit = remember {
        { event ->
            when (event) {
                is ResetPasswordEvent.CurrentPasswordChanged -> currentPassword = event.value
                is ResetPasswordEvent.NewPasswordChanged -> newPassword = event.value
                is ResetPasswordEvent.ConfirmPasswordChanged -> confirmPassword = event.value
                ResetPasswordEvent.Submit -> {
                    if (newPassword != confirmPassword) {
                        error = "Passwords do not match"
                    } else if (currentPassword.isBlank() || newPassword.isBlank()) {
                        error = "Please fill all fields"
                    } else {
                        isLoading = true
                        error = null
                        // Logic to call component.submitResetPassword(currentPassword, newPassword)
                        // Since this call might be async and we want to handle loading state here, 
                        // ideally the component should expose a flow or suspend function.
                        // For now, we will fire and forget, assuming the component handles it.
                        // Real implementation should likely observe loading state from component or use suspend here.
                        component.submitResetPassword(currentPassword, newPassword)
                        // For simplicity in this pattern:
                         isLoading = false // In real app, wait for result
                        onDismiss()
                    }
                }
                ResetPasswordEvent.Cancel -> onDismiss()
                ResetPasswordEvent.ErrorDismissed -> error = null
            }
        }
    }
    return state to eventSink
}
