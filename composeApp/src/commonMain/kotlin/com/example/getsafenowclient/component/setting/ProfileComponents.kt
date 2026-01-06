package com.example.getsafenowclient.component.setting

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.getsafenowclient.auth.PasswordValidationState
import com.example.getsafenowclient.auth.validatePassword
import com.example.getsafenowclient.component.GsnPasswordTextField
import com.example.getsafenowclient.component.PasswordValidationView
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Regular
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.regular.TimesCircle
import compose.icons.fontawesomeicons.solid.ChevronLeft
import io.getsafenow.libraries.designcomponents.utils.preview.GsnPreview
import io.getsafenow.libraries.gsn_theme.customtheme.GsnTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

// ---------------------------------------------------------
// 1. Header Component
// ---------------------------------------------------------
@Composable
fun ProfileHeader(
    onBackClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back Button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = FontAwesomeIcons.Solid.ChevronLeft,
                contentDescription = "Back",
                tint = GsnTheme.colors.iconPrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title
        Text(
            text = "Profile",
            style = GsnTheme.typography.fontHeadingMdBold,
            color = GsnTheme.colors.textPrimary
        )

        Spacer(modifier = Modifier.weight(1f))

        // Close Button
        IconButton(
            onClick = onCloseClick,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = FontAwesomeIcons.Regular.TimesCircle,
                contentDescription = "Close",
                tint = GsnTheme.colors.iconSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ---------------------------------------------------------
// 2. Field Component
// ---------------------------------------------------------
@Composable
fun ProfileFieldItem(
    label: String,
    value: String,
    onEditClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Label
        Text(
            text = label,
            style = GsnTheme.typography.fontBodyMdMedium,
            color = GsnTheme.colors.textPrimary
        )

        // Value Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = value,
                style = GsnTheme.typography.fontBodyLgRegular,
                color = GsnTheme.colors.textSecondary,
                modifier = Modifier.weight(1f)
            )

            if (onEditClick != null) {
                OutlinedButton(
                    onClick = onEditClick,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, GsnTheme.colors.borderInteractiveSecondary),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = GsnTheme.colors.textPrimary,
                        containerColor = Color.Transparent
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = "Edit",
                        style = GsnTheme.typography.fontBodyMdMedium,
                        color = GsnTheme.colors.textPrimary
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------
// 2.5 Editable Field Component (NEW)
// ---------------------------------------------------------
@Composable
fun ProfileEditableField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Label
        Text(
            text = label,
            style = GsnTheme.typography.fontBodyMdMedium,
            color = GsnTheme.colors.textPrimary
        )

        // Text Field
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = GsnTheme.colors.bgSubtleSecondary,
                unfocusedContainerColor = GsnTheme.colors.bgSubtleSecondary,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                cursorColor = GsnTheme.colors.textPrimary,
                focusedTextColor = GsnTheme.colors.textPrimary,
                unfocusedTextColor = GsnTheme.colors.textPrimary
            ),
            textStyle = GsnTheme.typography.fontBodyLgRegular,
            singleLine = true
        )

        // Buttons Row
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Save Button
            Button(
                onClick = onSave,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GsnTheme.colors.textPrimary, // Dark/Black from theme
                    contentColor = GsnTheme.colors.bgCanvasDefault
                ),
                modifier = Modifier.height(36.dp)
            ) {
                Text(
                    text = "Save",
                    style = GsnTheme.typography.fontBodyMdMedium
                )
            }

            // Cancel Button
            OutlinedButton(
                onClick = onCancel,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, GsnTheme.colors.borderInteractiveSecondary),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = GsnTheme.colors.textPrimary,
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.height(36.dp)
            ) {
                Text(
                    text = "Cancel",
                    style = GsnTheme.typography.fontBodyMdMedium
                )
            }
        }
    }
}


// ---------------------------------------------------------
// 3. Action Buttons
// ---------------------------------------------------------
@Composable
fun ProfileActions(
    onResetPasswordClick: () -> Unit,
    onSignOutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Reset Password
        OutlinedButton(
            onClick = onResetPasswordClick,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, GsnTheme.colors.borderInteractiveSecondary),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = GsnTheme.colors.textPrimary,
                containerColor = Color.Transparent
            )
        ) {
            Text(
                text = "Reset Password",
                style = GsnTheme.typography.fontBodyLgMedium
            )
        }

        // Sign Out
        Button(
            onClick = onSignOutClick,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GsnTheme.colors.bgCriticalPrimary,
                contentColor = GsnTheme.colors.textOnSolidPrimary
            )
        ) {
            Text(
                text = "Sign Out",
                style = GsnTheme.typography.fontBodyLgMedium
            )
        }
    }
}

// ---------------------------------------------------------
// 4. Reset Password Dialog
// ---------------------------------------------------------
@Composable
fun ResetPasswordDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String) -> Unit,
    isLoading: Boolean = false,
    error: String? = null
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // State for password validation
    var passwordValidationState by remember { mutableStateOf(PasswordValidationState()) }
    var isNewPasswordFocused by remember { mutableStateOf(false) }

    // Error state for current password (local validation)
    var currentPasswordLocalError by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = GsnTheme.colors.bgCanvasDefault),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Reset Password",
                        style = GsnTheme.typography.fontHeadingMdBold,
                        color = GsnTheme.colors.textPrimary,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.TopEnd).size(24.dp)
                    ) {
                        Icon(
                            imageVector = FontAwesomeIcons.Regular.TimesCircle,
                            contentDescription = "Close",
                            tint = GsnTheme.colors.iconSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Description
                Text(
                    text = "Enter your current password and choose a new password",
                    style = GsnTheme.typography.fontBodyMdRegular,
                    color = GsnTheme.colors.textSecondary,
                    textAlign = TextAlign.Center
                )

                // Removed standalone error Text here to make it inline with the field

                // Inputs
                GsnPasswordTextField(
                    value = currentPassword,
                    onValueChange = {
                        currentPassword = it
                        currentPasswordLocalError = null
                    },
                    label = "Current Password",
                    placeholder = "Enter current password",
                    error = currentPasswordLocalError ?: error, // Show local error OR server error here
                    modifier = Modifier.fillMaxWidth()
                )

                GsnPasswordTextField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        passwordValidationState = validatePassword(it)
                    },
                    label = "New Password",
                    placeholder = "Enter new password",
                    modifier = Modifier.fillMaxWidth().onFocusChanged { isNewPasswordFocused = it.isFocused }
                )

                // Password Validation View
                if (isNewPasswordFocused || newPassword.isNotEmpty()) {
                    PasswordValidationView(validationState = passwordValidationState)
                }

                GsnPasswordTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Confirm New Password",
                    placeholder = "Re-enter new password",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Submit Button
                Button(
                    onClick = {
                        if (currentPassword.isBlank()) {
                            currentPasswordLocalError = "Current password is required"
                        } else {
                            onSubmit(currentPassword, newPassword)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GsnTheme.colors.textPrimary, // Matches the dark grey look in image
                        contentColor = GsnTheme.colors.bgCanvasDefault
                    ),
                    enabled = !isLoading && currentPassword.isNotBlank() && newPassword.isNotBlank() && confirmPassword == newPassword && passwordValidationState.isValid
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = GsnTheme.colors.bgCanvasDefault,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Reset Password",
                            style = GsnTheme.typography.fontBodyLgMedium
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------
// 5. Sign Out Confirmation Dialog
// ---------------------------------------------------------
@Composable
fun SignOutConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = GsnTheme.colors.bgCanvasDefault),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Sign Out",
                    style = GsnTheme.typography.fontHeadingMdBold,
                    color = GsnTheme.colors.textPrimary
                )
                Text(
                    text = "Are you sure you want to sign out?",
                    style = GsnTheme.typography.fontBodyMdRegular,
                    color = GsnTheme.colors.textSecondary,
                    textAlign = TextAlign.Center
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, GsnTheme.colors.borderInteractiveSecondary),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = GsnTheme.colors.textPrimary,
                            containerColor = Color.Transparent
                        )
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GsnTheme.colors.bgCriticalPrimary,
                            contentColor = GsnTheme.colors.textOnSolidPrimary
                        )
                    ) {
                        Text("Sign Out")
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------
// 6. Assembled Screen Content
// ---------------------------------------------------------
@Composable
fun ProfileScreenContent(
    username: String,
    displayName: String,
    email: String,
    onBack: () -> Unit,
    onClose: () -> Unit,
    onEditDisplayName: () -> Unit,
    onEditEmail: () -> Unit,
    onResetPassword: () -> Unit,
    onSignOut: () -> Unit,

    // Add edit state params
    isEditingDisplayName: Boolean = false,
    tempDisplayName: String = "",
    onDisplayNameChange: (String) -> Unit = {},
    onSaveDisplayName: () -> Unit = {},
    onCancelEditDisplayName: () -> Unit = {},

    isEditingEmail: Boolean = false,
    tempEmail: String = "",
    onEmailChange: (String) -> Unit = {},
    onSaveEmail: () -> Unit = {},
    onCancelEditEmail: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(GsnTheme.colors.bgCanvasDefault)
    ) {
        // Header
        ProfileHeader(onBackClick = onBack, onCloseClick = onClose)

        HorizontalDivider(color = GsnTheme.colors.borderInteractiveSecondary, thickness = 1.dp)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Username first (as per Figma)
            ProfileFieldItem(
                label = "Username",
                value = username,
                onEditClick = null // Username usually not editable
            )

            // Display Name second
            if (isEditingDisplayName) {
                ProfileEditableField(
                    label = "Display Name",
                    value = tempDisplayName,
                    onValueChange = onDisplayNameChange,
                    onSave = onSaveDisplayName,
                    onCancel = onCancelEditDisplayName
                )
            } else {
                ProfileFieldItem(
                    label = "Display Name",
                    value = displayName,
                    onEditClick = onEditDisplayName
                )
            }

            // Email third
            if (isEditingEmail) {
                ProfileEditableField(
                    label = "Email",
                    value = tempEmail,
                    onValueChange = onEmailChange,
                    onSave = onSaveEmail,
                    onCancel = onCancelEditEmail
                )
            } else {
                ProfileFieldItem(
                    label = "Email",
                    value = email,
                    onEditClick = onEditEmail
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            ProfileActions(
                onResetPasswordClick = onResetPassword,
                onSignOutClick = onSignOut
            )
        }
    }
}

@Preview
@Composable
private fun ProfileScreenPreview() {
    GsnPreview {
        ProfileScreenContent(
            username = "@bhavuk:example.com",
            displayName = "Bhavuk",
            email = "bhavuk@example.com",
            onBack = {},
            onClose = {},
            onEditDisplayName = {},
            onEditEmail = {},
            onResetPassword = {},
            onSignOut = {}
        )
    }
}
