package com.example.getsafenowclient.home.setting.profile

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.getsafenowclient.component.setting.ProfileScreenContent
import com.example.getsafenowclient.component.setting.ResetPasswordDialog
import com.example.getsafenowclient.component.setting.SignOutConfirmationDialog
import com.example.getsafenowclient.service.SessionManager
import io.getsafenow.libraries.gsn_theme.customtheme.GsnTheme

@Composable
fun ProfileScreen(
    profileComponent: ProfileComponent,
    sessionManager: SessionManager,
    onBack: () -> Unit,
    onClose: () -> Unit,
    onSignOutComplete: () -> Unit
) {
    val (state, eventSink) = profilePresenter(
        profileComponent = profileComponent,
        sessionManager = sessionManager,
        onBack = onBack,
        onClose = onClose,
        onSignOutComplete = onSignOutComplete
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = GsnTheme.colors.bgCanvasDefault
    ) {
        ProfileScreenContent(
            username = state.username,
            displayName = state.displayName,
            email = state.email,
            onBack = { eventSink(ProfileEvent.Back) },
            onClose = { eventSink(ProfileEvent.Close) },
            onEditDisplayName = { eventSink(ProfileEvent.EditDisplayName) },
            onEditEmail = { eventSink(ProfileEvent.EditEmail) },
            onResetPassword = { eventSink(ProfileEvent.ResetPassword) },
            onSignOut = { eventSink(ProfileEvent.SignOutClicked) },

            // Edit Display Name Wiring
            isEditingDisplayName = state.isEditingDisplayName,
            tempDisplayName = state.tempDisplayName,
            onDisplayNameChange = { eventSink(ProfileEvent.UpdateDisplayName(it)) },
            onSaveDisplayName = { eventSink(ProfileEvent.SaveDisplayName) },
            onCancelEditDisplayName = { eventSink(ProfileEvent.CancelEditDisplayName) },

            // Edit Email Wiring
            isEditingEmail = state.isEditingEmail,
            tempEmail = state.tempEmail,
            onEmailChange = { eventSink(ProfileEvent.UpdateEmail(it)) },
            onSaveEmail = { eventSink(ProfileEvent.SaveEmail) },
            onCancelEditEmail = { eventSink(ProfileEvent.CancelEditEmail) }
        )

        if (state.isResetPasswordDialogVisible) {
            ResetPasswordDialog(
                onDismiss = { eventSink(ProfileEvent.DismissResetPasswordDialog) },
                onSubmit = { current, new ->
                    eventSink(ProfileEvent.SubmitResetPassword(current, new))
                },
                isLoading = state.isResetPasswordLoading,
                error = state.resetPasswordError
            )
        }

        if (state.isSignOutDialogVisible) {
            SignOutConfirmationDialog(
                onDismiss = { eventSink(ProfileEvent.DismissSignOutDialog) },
                onConfirm = { eventSink(ProfileEvent.ConfirmSignOut) }
            )
        }
    }
}
