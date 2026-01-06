package com.example.getsafenowclient.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.getsafenowclient.auth.PasswordValidationState
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Regular
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.regular.CheckCircle
import compose.icons.fontawesomeicons.regular.TimesCircle
import compose.icons.fontawesomeicons.solid.Check
import compose.icons.fontawesomeicons.solid.Eye
import compose.icons.fontawesomeicons.solid.EyeSlash
import compose.icons.fontawesomeicons.solid.Times
import io.getsafenow.libraries.designcomponents.utils.preview.GsnPreview
import io.getsafenow.libraries.gsn_theme.customtheme.GsnTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * A segmented control to toggle between the Login and Sign Up screens.
 *
 * @param selectedScreen The currently selected screen, either [OnboardingScreens.Login] or [OnboardingScreens.SignUp].
 * @param onScreenSelected A callback invoked when the user selects a different screen.
 * @param modifier The modifier to be applied to the component.
 */
@Composable
fun AuthScreenToggle(
    selectedScreen: OnboardingScreens,
    onScreenSelected: (OnboardingScreens) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(26.dp),
        color = GsnTheme.colors.bgSubtleSecondary
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AuthToggleButton(
                text = "Login",
                isSelected = selectedScreen == OnboardingScreens.Login,
                onClick = { onScreenSelected(OnboardingScreens.Login) },
                modifier = Modifier.weight(1f)
            )
            AuthToggleButton(
                text = "Sign Up",
                isSelected = selectedScreen == OnboardingScreens.SignUp,
                onClick = { onScreenSelected(OnboardingScreens.SignUp) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthToggleButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) GsnTheme.colors.bgCanvasDefault else Color.Transparent
    val textColor = if (isSelected) GsnTheme.colors.textPrimary else GsnTheme.colors.textSecondary
    val elevation = if (isSelected) 2.dp else 0.dp

    Card(
        modifier = modifier.fillMaxHeight(),
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = GsnTheme.typography.fontBodyLgMedium,
                color = textColor,
            )
        }
    }
}

/**
 * A header component for authentication screens that displays a title based on the selected screen.
 *
 * @param selectedScreen The currently selected screen, either [OnboardingScreens.Login] or [OnboardingScreens.SignUp].
 * @param modifier The modifier to be applied to the component.
 */
@Composable
fun AuthHeader(
    selectedScreen: OnboardingScreens,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val title = when (selectedScreen) {
            OnboardingScreens.Login -> "Welcome Back"
            else -> "Welcome"
        }
        val subtitle = when (selectedScreen) {
            OnboardingScreens.Login -> "Sign in to your account"
            else -> "Create your account"
        }
        Text(
            text = title,
            style = GsnTheme.typography.fontHeadingLgBold,
            color = GsnTheme.colors.textPrimary
        )
        Text(
            text = subtitle,
            style = GsnTheme.typography.fontBodyLgRegular,
            color = GsnTheme.colors.textSecondary
        )
    }
}

/**
 * A styled OutlinedTextField with a label positioned above it, conforming to the GSN theme.
 *
 * @param value The input text to be shown in the text field.
 * @param onValueChange The callback that is triggered when the input service updates the text.
 * @param label The text to be displayed as the label above the text field.
 * @param modifier The modifier to be applied to the component.
 * @param error An optional error message to display below the text field.
 * @param placeholder The optional placeholder to be displayed when the text field is empty.
 * @param keyboardOptions Software keyboard options that contains configuration such as [KeyboardType].
 * @param visualTransformation The visual transformation to apply to the input text.
 * @param trailingIcon The optional trailing icon to be displayed at the end of the text field.
 */
@Composable
fun GsnLabeledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    placeholder: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = GsnTheme.typography.fontBodyMdMedium,
            color = GsnTheme.colors.textPrimary
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                if (placeholder != null) {
                    Text(
                        text = placeholder,
                        style = GsnTheme.typography.fontBodyLgRegular,
                        color = GsnTheme.colors.textSecondary
                    )
                }
            },
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = GsnTheme.colors.bgSubtleSecondary,
                unfocusedContainerColor = GsnTheme.colors.bgSubtleSecondary,
                focusedBorderColor = GsnTheme.colors.bgAccentRest,
                unfocusedBorderColor = Color.Transparent,
                cursorColor = GsnTheme.colors.bgAccentRest,
                focusedTextColor = GsnTheme.colors.textPrimary,
                unfocusedTextColor = GsnTheme.colors.textPrimary,
                errorBorderColor = GsnTheme.colors.borderCriticalPrimary,
                errorCursorColor = GsnTheme.colors.bgAccentRest,
                errorTextColor = GsnTheme.colors.textPrimary,
                errorSupportingTextColor = GsnTheme.colors.textCriticalPrimary,
                errorLabelColor = GsnTheme.colors.textPrimary,
            ),
            textStyle = GsnTheme.typography.fontBodyLgMedium,
            visualTransformation = visualTransformation,
            trailingIcon = trailingIcon,
            keyboardOptions = keyboardOptions,
            singleLine = true,
            isError = error != null,
            supportingText = {
                if (error != null) {
                    Text(
                        text = error,
                        style = GsnTheme.typography.fontBodyMdRegular
                    )
                }
            }
        )
    }
}

/**
 * A styled password text field with a visibility toggle, conforming to the GSN theme.
 *
 * @param value The input password to be shown in the text field.
 * @param onValueChange The callback that is triggered when the input service updates the password.
 * @param label The text to be displayed as the label above the text field.
 * @param modifier The modifier to be applied to the component.
 * @param error An optional error message to display below the text field.
 * @param placeholder The optional placeholder to be displayed when the text field is empty.
 */
@Composable
fun GsnPasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    placeholder: String? = null,
) {
    var passwordVisible by remember { mutableStateOf(false) }

    GsnLabeledTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        placeholder = placeholder,
        error = error,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            val image = if (passwordVisible) FontAwesomeIcons.Solid.EyeSlash else FontAwesomeIcons.Solid.Eye
            val description = if (passwordVisible) "Hide password" else "Show password"
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    imageVector = image,
                    contentDescription = description,
                    tint = GsnTheme.colors.iconSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    )
}

/**
 * A centralized, full-width authentication button styled according to the GSN theme.
 *
 * @param onClick The callback to be invoked when this button is clicked.
 * @param modifier The modifier to be applied to the button.
 * @param enabled Controls the enabled state of the button. When `false`, this button will not be clickable.
 * @param content The content to display inside the button.
 */
@Composable
fun GsnAuthButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(42.dp),
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = GsnTheme.colors.bgActionPrimaryRest,
            contentColor = GsnTheme.colors.textOnSolidPrimary,
            disabledContainerColor = GsnTheme.colors.bgActionPrimaryDisabled,
            disabledContentColor = GsnTheme.colors.textDisabled
        ),
        content = content
    )
}

/**
 * A composable that displays a checkbox followed by text with a clickable link.
 *
 * @param checked The checked state of the checkbox.
 * @param onCheckedChange The callback that is triggered when the checkbox is checked or unchecked.
 * @param plainText The non-clickable part of the text.
 * @param linkText The clickable part of the text.
 * @param linkUrl The URL to open when the link is clicked.
 * @param modifier The modifier to be applied to the component.
 */
@Composable
fun GsnClickableTextCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    plainText: String,
    linkText: String,
    linkUrl: String,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    val annotatedString = buildAnnotatedString {
        append(plainText)
        pushStringAnnotation(tag = "URL", annotation = linkUrl)
        withStyle(style = SpanStyle(color = GsnTheme.colors.textActionAccent)) {
            append(linkText)
        }
        pop()
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = androidx.compose.material3.CheckboxDefaults.colors(
                checkedColor = GsnTheme.colors.iconAccentPrimary,
                uncheckedColor = GsnTheme.colors.iconSecondary,
            )
        )
        ClickableText(
            text = annotatedString,
            style = GsnTheme.typography.fontBodyMdRegular.copy(color = GsnTheme.colors.textSecondary),
            onClick = {
                annotatedString.getStringAnnotations(tag = "URL", start = it, end = it)
                    .firstOrNull()?.let {
                        uriHandler.openUri(it.item)
                    }
            }
        )
    }
}

@Composable
fun PasswordValidationView(validationState: PasswordValidationState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = GsnTheme.colors.bgSubtleSecondary),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Password must contain:",
                style = GsnTheme.typography.fontBodyMdMedium,
                color = GsnTheme.colors.textPrimary
            )
            PasswordRequirement(text = "At least 8 characters", isValid = validationState.hasMinLength)
            PasswordRequirement(text = "One uppercase letter", isValid = validationState.hasUppercase)
            PasswordRequirement(text = "One lowercase letter", isValid = validationState.hasLowercase)
            PasswordRequirement(text = "One number", isValid = validationState.hasNumber)
            PasswordRequirement(
                text = "One special character (!@#$%^&*...)",
                isValid = validationState.hasSpecialChar
            )
        }
    }
}

@Composable
private fun PasswordRequirement(text: String, isValid: Boolean) {
    val icon = if (isValid) FontAwesomeIcons.Regular.CheckCircle else FontAwesomeIcons.Regular.TimesCircle
    val textColor = if (isValid) GsnTheme.colors.textDecorative1 else GsnTheme.colors.textSecondary
    val iconColor = if (isValid) GsnTheme.colors.textDecorative1 else GsnTheme.colors.textSecondary

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = if (isValid) "Requirement met" else "Requirement not met",
            tint = iconColor,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            color = textColor,
            style = GsnTheme.typography.fontBodyMdRegular
        )
    }
}


@Preview
@Composable
private fun AuthComponentsPreview() {
    GsnPreview {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            var selectedScreen by remember { mutableStateOf(OnboardingScreens.Login) }
            AuthScreenToggle(
                selectedScreen = selectedScreen,
                onScreenSelected = { selectedScreen = it })
            AuthHeader(selectedScreen = selectedScreen)
            GsnLabeledTextField(
                value = "",
                onValueChange = {},
                label = "Username",
                placeholder = "Enter your username",
                error = "This username is already taken."
            )
            GsnPasswordTextField(
                value = "password",
                onValueChange = {},
                label = "Password",
                placeholder = "Enter your password"
            )
            GsnAuthButton(onClick = {}) {
                Text("Sign In")
            }
            var termsChecked by remember { mutableStateOf(false) }
            GsnClickableTextCheckbox(
                checked = termsChecked,
                onCheckedChange = { termsChecked = it },
                plainText = "I agree to the ",
                linkText = "Terms and Conditions",
                linkUrl = "https://example.com"
            )
            PasswordValidationView(validationState = PasswordValidationState(
                hasMinLength = true,
                hasUppercase = true,
                hasLowercase = false
            ))
        }
    }
}
