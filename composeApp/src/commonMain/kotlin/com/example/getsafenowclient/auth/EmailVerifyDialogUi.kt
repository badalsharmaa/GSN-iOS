package com.example.getsafenowclient.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import io.getsafenow.libraries.architecture.AsyncAction
import io.getsafenow.libraries.gsn_theme.customtheme.GsnTheme

/**
 * A dialog that prompts the user to verify their email address after registration.
 *
 * @param state The current authentication state.
 * @param eventSink The callback to handle user events.
 */
@Composable
fun EmailVerifyDialogUi(
    state: AuthState,
    eventSink: (AuthEvent) -> Unit,
) {
    if (state.isEmailVerifyDialogVisible) {
        AlertDialog(
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            ),
            onDismissRequest = {
                // Not to add this
            },
            title = {
                Text(
                    text = "Verify Your Email",
                    style = GsnTheme.materialTypography.headlineSmall,
                    color = GsnTheme.colors.textPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "We\'ve sent a verification link to your email address. Please click the link, then press the button below to continue.",
                        style = GsnTheme.materialTypography.bodyMedium,
                        color = GsnTheme.colors.textSecondary
                    )
                    if (state.completeVerifyAction is AsyncAction.Failure) {
                        Text(
                            text = state.completeVerifyAction.error.message ?: "An unexpected error occurred during verification on EmailVerifyDialogUi.",
                            color = GsnTheme.colors.textCriticalPrimary,
                            style = GsnTheme.typography.fontBodyMdRegular,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { eventSink(AuthEvent.CompleteEmailVerificationClicked) },
                    enabled = state.completeVerifyAction !is AsyncAction.Loading
                ) {
                    if (state.completeVerifyAction is AsyncAction.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = GsnTheme.colors.iconAccentPrimary
                        )
                    } else {
                        Text("I Have Verified")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { eventSink(AuthEvent.DismissEmailVerifyDialog) }) {
                    Text("Cancel")
                }
            },
            containerColor = GsnTheme.colors.bgCanvasDefault,
            titleContentColor = GsnTheme.colors.textPrimary,
            textContentColor = GsnTheme.colors.textSecondary
        )
    }
}
