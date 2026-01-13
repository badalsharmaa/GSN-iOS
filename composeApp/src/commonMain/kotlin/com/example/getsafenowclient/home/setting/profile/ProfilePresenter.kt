package com.example.getsafenowclient.home.setting.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.getsafenowclient.service.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun profilePresenter(
    profileComponent: ProfileComponent,
    sessionManager: SessionManager,
    onBack: () -> Unit,
    onClose: () -> Unit,
    onSignOutComplete: () -> Unit
): Pair<ProfileState, (ProfileEvent) -> Unit> {

    val userProfile by profileComponent.userProfile.collectAsState(initial = null)
    var isResetPasswordDialogVisible by remember { mutableStateOf(false) }
    var isSignOutDialogVisible by remember { mutableStateOf(false) }

    // Editing States
    var isEditingDisplayName by remember { mutableStateOf(false) }
    var tempDisplayName by remember { mutableStateOf("") }

    var isEditingEmail by remember { mutableStateOf(false) }
    var tempEmail by remember { mutableStateOf("") }

    // Reset Password States
    var isResetPasswordLoading by remember { mutableStateOf(false) }
    var resetPasswordError by remember { mutableStateOf<String?>(null) }

    val state = ProfileState(
        username = userProfile?.userId ?: "",
        displayName = userProfile?.displayName ?: "",
        email = userProfile?.email ?: "",
        avatarUrl = userProfile?.avatarUrl,
        isLoading = userProfile == null,
        isResetPasswordDialogVisible = isResetPasswordDialogVisible,
        isSignOutDialogVisible = isSignOutDialogVisible,
        isEditingDisplayName = isEditingDisplayName,
        tempDisplayName = tempDisplayName,
        isEditingEmail = isEditingEmail,
        tempEmail = tempEmail,
        isResetPasswordLoading = isResetPasswordLoading,
        resetPasswordError = resetPasswordError
    )

    val scope = remember { CoroutineScope(Dispatchers.Main) }

    val eventSink: (ProfileEvent) -> Unit = remember {
        { event ->
            when (event) {
                ProfileEvent.Back -> onBack()
                ProfileEvent.Close -> onClose()
                
                // Display Name
                ProfileEvent.EditDisplayName -> {
                    tempDisplayName = userProfile?.displayName ?: ""
                    isEditingDisplayName = true
                }
                is ProfileEvent.UpdateDisplayName -> {
                    tempDisplayName = event.newName
                }
                ProfileEvent.SaveDisplayName -> {
                    isEditingDisplayName = false
                    profileComponent.updateDisplayName(tempDisplayName)
                }
                ProfileEvent.CancelEditDisplayName -> {
                    isEditingDisplayName = false
                }

                // Email
                ProfileEvent.EditEmail -> {
                    tempEmail = userProfile?.email ?: ""
                    isEditingEmail = true
                }
                is ProfileEvent.UpdateEmail -> {
                    tempEmail = event.newEmail
                }
                ProfileEvent.SaveEmail -> {
                    isEditingEmail = false
                    profileComponent.updateEmail(tempEmail)
                }
                ProfileEvent.CancelEditEmail -> {
                    isEditingEmail = false
                }

                ProfileEvent.ResetPassword -> {
                    isResetPasswordDialogVisible = true
                    resetPasswordError = null
                }
                ProfileEvent.SignOutClicked -> {
                    isSignOutDialogVisible = true
                }
                ProfileEvent.DismissSignOutDialog -> {
                    isSignOutDialogVisible = false
                }
                ProfileEvent.ConfirmSignOut -> {
                    isSignOutDialogVisible = false
                    scope.launch {
                        sessionManager.logout()
                        onSignOutComplete()
                    }
                }
                ProfileEvent.DismissResetPasswordDialog -> {
                    isResetPasswordDialogVisible = false
                    resetPasswordError = null
                    isResetPasswordLoading = false
                }
                is ProfileEvent.SubmitResetPassword -> {
                    scope.launch {
                        isResetPasswordLoading = true
                        resetPasswordError = null
                        profileComponent.resetPassword(event.current, event.new)
                            .onSuccess {
                                isResetPasswordDialogVisible = false
                            }
                            .onFailure { e ->
                                resetPasswordError = e.message ?: "Failed to reset password"
                            }
                        isResetPasswordLoading = false
                    }
                }
            }
        }
    }

    return state to eventSink
}
