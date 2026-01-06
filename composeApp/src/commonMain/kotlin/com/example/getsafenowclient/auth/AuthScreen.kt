package com.example.getsafenowclient.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import com.example.getsafenowclient.component.AuthHeader
import com.example.getsafenowclient.component.AuthScreenToggle
import com.example.getsafenowclient.component.GsnAuthButton
import com.example.getsafenowclient.component.GsnClickableTextCheckbox
import com.example.getsafenowclient.component.GsnLabeledTextField
import com.example.getsafenowclient.component.GsnPasswordTextField
import com.example.getsafenowclient.component.OnboardingScreens
import com.example.getsafenowclient.component.PasswordValidationView
import com.example.getsafenowclient.generated.resources.Res
import com.example.getsafenowclient.generated.resources.app_analytics_icon
import io.getsafenow.libraries.architecture.AsyncAction
import io.getsafenow.libraries.designcomponents.utils.preview.GsnPreview
import io.getsafenow.libraries.gsn_theme.customtheme.GsnTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun AuthScreen(
    state: AuthState,
    eventSink: (AuthEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = GsnTheme.colors.bgCanvasDefault
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 48.dp, start = 24.dp, end = 24.dp)
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Image(
                    painter = painterResource(Res.drawable.app_analytics_icon),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(80.dp)
                )

                Spacer(modifier = Modifier.height(5.dp))

                AuthHeader(selectedScreen = state.selectedScreen)

                Spacer(modifier = Modifier.height(5.dp))

                AuthScreenToggle(
                    selectedScreen = state.selectedScreen,
                    onScreenSelected = { eventSink(AuthEvent.ToggleScreen(it)) }
                )

                Spacer(modifier = Modifier.height(5.dp))

                AnimatedContent(
                    targetState = state.selectedScreen,
                    transitionSpec = {
                        val enter = if (targetState == OnboardingScreens.Login) {
                            slideInHorizontally(animationSpec = tween(300)) { -it } + fadeIn(animationSpec = tween(300))
                        } else {
                            slideInHorizontally(animationSpec = tween(300)) { it } + fadeIn(animationSpec = tween(300))
                        }
                        val exit = if (initialState == OnboardingScreens.Login) {
                            slideOutHorizontally(animationSpec = tween(300)) { it } + fadeOut(animationSpec = tween(300))
                        } else {
                            slideOutHorizontally(animationSpec = tween(300)) { -it } + fadeOut(animationSpec = tween(300))
                        }
                        enter togetherWith exit
                    },
                    label = "AuthFormAnimation"
                ) { screen ->
                    when (screen) {
                        OnboardingScreens.Login -> LoginContent(state, eventSink)
                        OnboardingScreens.SignUp -> SignUpContent(state, eventSink)
                        else -> Unit // Should not happen
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            EmailVerifyDialogUi(state = state, eventSink = eventSink)
        }
    }
}

@Composable
private fun LoginContent(
    state: AuthState,
    eventSink: (AuthEvent) -> Unit,
) {
    val isButtonEnabled = state.username.isNotBlank() && state.password.isNotBlank() && state.loginAction !is AsyncAction.Loading

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GsnLabeledTextField(
            value = state.username,
            onValueChange = { eventSink(AuthEvent.UpdateUsername(it)) },
            label = "Username",
            placeholder = "Enter your username",
            error = state.usernameError
        )
        GsnPasswordTextField(
            value = state.password,
            onValueChange = { eventSink(AuthEvent.UpdatePassword(it)) },
            label = "Password",
            placeholder = "Enter your password",
            error = state.passwordError
        )

        if (state.genericError != null) {
            Text(
                text = state.genericError,
                color = GsnTheme.colors.textCriticalPrimary,
                style = GsnTheme.typography.fontBodyMdRegular,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        GsnAuthButton(
            onClick = { eventSink(AuthEvent.LoginClicked) },
            enabled = isButtonEnabled
        ) {
            if (state.loginAction is AsyncAction.Loading) {
                CircularProgressIndicator(
                    color = GsnTheme.colors.iconOnSolidPrimary,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Sign In",
                    style = GsnTheme.typography.fontBodyLgMedium
                )
            }
        }

        TextButton(onClick = { /*TODO: Forgot Password flow*/ }) {
            Text(
                text = "Forgot password?",
                style = GsnTheme.typography.fontBodyMdMedium,
                color = GsnTheme.colors.textActionAccent
            )
        }
    }
}

@Composable
private fun SignUpContent(
    state: AuthState,
    eventSink: (AuthEvent) -> Unit,
) {
    val isButtonEnabled = state.email.isNotBlank() &&
            state.username.isNotBlank() &&
            state.passwordValidationState.isValid &&
            state.termsAccepted &&
            state.privacyAccepted &&
            state.signUpAction !is AsyncAction.Loading

    // State to track if the password field is focused
    var isPasswordFocused by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GsnLabeledTextField(
            value = state.email,
            onValueChange = { eventSink(AuthEvent.UpdateEmail(it)) },
            label = "Email *",
            placeholder = "you@example.com",
            error = state.emailError
        )
        GsnLabeledTextField(
            value = state.username,
            onValueChange = { eventSink(AuthEvent.UpdateUsername(it)) },
            label = "Username *",
            placeholder = "username",
            error = state.usernameError
        )
        GsnPasswordTextField(
            value = state.password,
            onValueChange = { eventSink(AuthEvent.UpdatePassword(it)) },
            label = "Password *",
            placeholder = "Create a password",
            error = state.passwordError,
            modifier = Modifier.onFocusChanged { focusState ->
                isPasswordFocused = focusState.isFocused
            }
        )

        // Only show validation view if the password field is focused OR if the password is not empty
        if (isPasswordFocused || state.password.isNotEmpty()) {
            PasswordValidationView(validationState = state.passwordValidationState)
        }

        if (state.genericError != null) {
            Text(
                text = state.genericError,
                color = GsnTheme.colors.textCriticalPrimary,
                style = GsnTheme.typography.fontBodyMdRegular,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        GsnClickableTextCheckbox(
            checked = state.termsAccepted,
            onCheckedChange = { eventSink(AuthEvent.ToggleTerms(it)) },
            plainText = "I have read and accept the ",
            linkText = "Terms and Conditions *",
            linkUrl = "https://onboarding.getsafenow.app/"
        )
        GsnClickableTextCheckbox(
            checked = state.privacyAccepted,
            onCheckedChange = { eventSink(AuthEvent.TogglePrivacy(it)) },
            plainText = "I have read and accept the ",
            linkText = "Privacy Policy *",
            linkUrl = "https://onboarding.getsafenow.app/"
        )

        GsnAuthButton(
            onClick = { eventSink(AuthEvent.SignUpClicked) },
            enabled = isButtonEnabled
        ) {
            if (state.signUpAction is AsyncAction.Loading) {
                CircularProgressIndicator(
                    color = GsnTheme.colors.iconOnSolidPrimary,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Create Account",
                    style = GsnTheme.typography.fontBodyLgMedium
                )
            }
        }
    }
}


@Preview
@Composable
private fun AuthScreenLoginPreview() {
    val previewState = AuthState(
        selectedScreen = OnboardingScreens.Login,
        loginAction = AsyncAction.Uninitialized,
        usernameError = "Invalid username."
    )
    GsnPreview {
        AuthScreen(state = previewState, eventSink = {})
    }
}

@Preview
@Composable
private fun AuthScreenSignUpPreview() {
    val previewState = AuthState(
        selectedScreen = OnboardingScreens.SignUp,
        loginAction = AsyncAction.Uninitialized,
        emailError = "This email is already in use."
    )
    GsnPreview {
        AuthScreen(state = previewState, eventSink = {})
    }
}
