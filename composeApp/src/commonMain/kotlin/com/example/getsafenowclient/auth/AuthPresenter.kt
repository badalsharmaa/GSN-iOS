package com.example.getsafenowclient.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.example.getsafenowclient.component.OnboardingScreens
import com.example.getsafenowclient.service.SessionManager
import io.getsafenow.libraries.architecture.AsyncAction
import io.getsafenow.libraries.architecture.runCatchingUpdatingState
import io.getsafenow.libraries.gsn_matrix.api.auth.AuthenticationException
import kotlinx.coroutines.launch

/**
 * The presenter for the authentication screen. It manages the screen's state and logic.
 *
 * @param sessionManager The manager responsible for handling login and session restoration.
 * @param onLoginSuccess A callback to be invoked upon a successful login.
 * @return A pair containing the current [AuthState] and the event sink lambda.
 */
@Composable
fun authPresenter(
    sessionManager: SessionManager,
    onLoginSuccess: () -> Unit,
): Pair<AuthState, (AuthEvent) -> Unit> {
    val coroutineScope = rememberCoroutineScope()

    // State variables
    val selectedScreen = remember { mutableStateOf(OnboardingScreens.Login) }
    val username = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val passwordValidationState = remember { mutableStateOf(PasswordValidationState()) }
    val termsAccepted = remember { mutableStateOf(false) }
    val privacyAccepted = remember { mutableStateOf(false) }
    val loginAction: MutableState<AsyncAction<Unit>> = remember { mutableStateOf(AsyncAction.Uninitialized) }
    val signUpAction: MutableState<AsyncAction<Unit>> = remember { mutableStateOf(AsyncAction.Uninitialized) }

    val isEmailVerifyDialogVisible = remember { mutableStateOf(false) }
    val pendingVerifyUsername = remember { mutableStateOf<String?>(null) }
    val pendingVerifyPassword = remember { mutableStateOf<String?>(null) }
    val completeVerifyAction = remember { mutableStateOf<AsyncAction<Unit>>(AsyncAction.Uninitialized) }

    // Inline error states
    val emailError = remember { mutableStateOf<String?>(null) }
    val usernameError = remember { mutableStateOf<String?>(null) }
    val passwordError = remember { mutableStateOf<String?>(null) }
    val genericError = remember { mutableStateOf<String?>(null) }

    fun clearErrors() {
        emailError.value = null
        usernameError.value = null
        passwordError.value = null
        genericError.value = null
    }

    // Side effect for successful login or sign-up
    LaunchedEffect(loginAction.value) {
        if (loginAction.value is AsyncAction.Success) {
            onLoginSuccess()
        }
    }

    val eventSink: (AuthEvent) -> Unit = eventSink@{ event ->
        when (event) {
            is AuthEvent.ToggleScreen -> {
                clearErrors()
                selectedScreen.value = event.screen
            }
            is AuthEvent.UpdateUsername -> {
                username.value = event.username
                usernameError.value = null
                genericError.value = null
            }
            is AuthEvent.UpdatePassword -> {
                password.value = event.password
                passwordValidationState.value = validatePassword(event.password)
                passwordError.value = null
                genericError.value = null
            }
            is AuthEvent.UpdateEmail -> {
                email.value = event.email
                emailError.value = null
                genericError.value = null
            }
            is AuthEvent.ToggleTerms -> termsAccepted.value = event.accepted
            is AuthEvent.TogglePrivacy -> privacyAccepted.value = event.accepted
            AuthEvent.LoginClicked -> {
                clearErrors()
                coroutineScope.launch {
                    loginAction.runCatchingUpdatingState {
                        sessionManager.login(
                            server = "https://spydefense.org/",
                            username = username.value,
                            password = password.value
                        )
                    }.onFailure { ex ->
                        val message = ex.message ?: "An unknown error occurred."
                        when (ex) {
                            is AuthenticationException.InvalidCredentials -> {
                                usernameError.value = message
                                passwordError.value = message
                            }
                            is AuthenticationException.NetworkError -> genericError.value = message
                            else -> genericError.value = message
                        }
                    }
                }
            }
            AuthEvent.SignUpClicked -> {
                clearErrors()
                if (!termsAccepted.value || !privacyAccepted.value) {
                    genericError.value = "Please accept the terms and privacy policy."
                    return@eventSink
                }
                coroutineScope.launch {
                    signUpAction.runCatchingUpdatingState {
                        sessionManager.registerAndVerifyEmail(
                            server = "https://spydefense.org/",
                            email = email.value,
                            username = username.value,
                            password = password.value
                        )
                    }.onSuccess { 
                        pendingVerifyUsername.value = username.value
                        pendingVerifyPassword.value = password.value
                        isEmailVerifyDialogVisible.value = true
                    }.onFailure { ex ->
                        val message = ex.message ?: "An unknown error occurred."
                        when (ex) {
                            is AuthenticationException.EmailExists -> {
                                if (message.contains("username", ignoreCase = true)) {
                                    usernameError.value = message
                                } else {
                                    emailError.value = message
                                }
                            }
                            is AuthenticationException.InvalidCredentials -> {
                                // Typically relates to username format in this context
                                usernameError.value = message
                            }
                            is AuthenticationException.InvalidServerName -> {
                                // Typically relates to email format (e.g. bad domain)
                                emailError.value = message
                            }
                            is AuthenticationException.NetworkError -> genericError.value = message
                            else -> genericError.value = message
                        }
                    }
                }
            }
            AuthEvent.DismissEmailVerifyDialog -> {
                isEmailVerifyDialogVisible.value = false
            }
            AuthEvent.CompleteEmailVerificationClicked -> {
                val user = pendingVerifyUsername.value
                val pass = pendingVerifyPassword.value
                if (user != null && pass != null) {
                    coroutineScope.launch {
                        completeVerifyAction.runCatchingUpdatingState {
                            val finalized = sessionManager.finalizeRegistration(
                                server = "https://spydefense.org/",
                                username = user,
                                password = pass
                            )
                            if (finalized) {
                                sessionManager.completeLoginAfterRegister(
                                    server = "https://spydefense.org/",
                                    username = user,
                                    password = pass
                                )
                            } else {
                                error("Registration still pending. Please verify your email and try again.")
                            }
                        }.onSuccess {
                            loginAction.value = AsyncAction.Success(Unit)
                            isEmailVerifyDialogVisible.value = false
                        }
                    }
                }
            }
        }
    }

    val state = AuthState(
        selectedScreen = selectedScreen.value,
        username = username.value,
        password = password.value,
        email = email.value,
        passwordValidationState = passwordValidationState.value,
        termsAccepted = termsAccepted.value,
        privacyAccepted = privacyAccepted.value,
        loginAction = loginAction.value,
        signUpAction = signUpAction.value,
        isEmailVerifyDialogVisible = isEmailVerifyDialogVisible.value,
        pendingVerifyUsername = pendingVerifyUsername.value,
        pendingVerifyPassword = pendingVerifyPassword.value,
        completeVerifyAction = completeVerifyAction.value,
        // errors
        emailError = emailError.value,
        usernameError = usernameError.value,
        passwordError = passwordError.value,
        genericError = genericError.value
    )
    return state to eventSink
}
