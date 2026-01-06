package com.example.getsafenowclient.home.setting.profile

import androidx.compose.runtime.Composable
import co.touchlab.kermit.Logger
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.example.getsafenowclient.matrixentensions.getUserEmail
import io.getsafenow.libraries.architecture.ScreenComponent
import io.getsafenow.libraries.gsn_matrix.api.auth.AuthenticationException
import io.getsafenow.libraries.gsn_matrix.impl.auth.mapAuthenticationException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.client.user
import net.folivo.trixnity.clientserverapi.client.UIA
import net.folivo.trixnity.clientserverapi.model.authentication.IdentifierType
import net.folivo.trixnity.clientserverapi.model.uia.AuthenticationRequest
import net.folivo.trixnity.core.ErrorResponse
import net.folivo.trixnity.core.MatrixServerException

interface ProfileComponent : ScreenComponent {
    val userProfile: Flow<ProfileUserData>
    
    // Methods to update profile data
    fun updateDisplayName(newName: String)
    fun updateEmail(newEmail: String)
    
    // Reset Password
    suspend fun resetPassword(current: String, new: String): Result<Unit>
}

data class ProfileUserData(
    val userId: String,
    val displayName: String,
    val avatarUrl: String?,
    val email: String,
)

class ProfileScreenModel(
    componentContext: ComponentContext,
    private val client: MatrixClient
) : ProfileComponent, ComponentContext by componentContext {

    private val scope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate).also { sc ->
            lifecycle.doOnDestroy { sc.cancel() }
        }

    private val emailFlow = MutableStateFlow("Loading…")

    init {
        scope.launch(Dispatchers.IO) {
            try {
                emailFlow.value = client.getUserEmail() ?: "No Email Present"
            } catch (e: Exception) {
                Logger.withTag("ProfileScreenModel").e("Error fetching email: $e")
                emailFlow.value = "Error loading email"
            }
        }
    }

    override val userProfile: Flow<ProfileUserData> =
        combine(
            client.displayName,
            client.avatarUrl,
            emailFlow
        ) { name, avatar, email ->
            ProfileUserData(
                userId = client.userId.localpart,
                displayName = name ?: client.userId.localpart,
                avatarUrl = avatar,
                email = email
            )
        }

    override fun updateDisplayName(newName: String) {
        scope.launch {
            try {
                client.setDisplayName(newName).getOrThrow()
            } catch (e: Exception) {
                // Handle error
                Logger.withTag("ProfileScreenModel").e("Error updating display name: $e")
            }
        }
    }

    override fun updateEmail(newEmail: String) {
        scope.launch {
            // Email update logic would go here
            // emailFlow.value = newEmail
        }
    }

    override suspend fun resetPassword(current: String, new: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val result = client.api.authentication.changePassword(
                newPassword = new
            ).getOrThrow()

            when (result) {
                is UIA.Success -> Result.success(Unit)
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
                        Result.success(Unit)
                    } else {
                        // Handle UIA failure (e.g. wrong password) or further steps
                        // Safe cast to UIA.Error to avoid property access issues on Step
                        val errorResponse = (authResponse as? UIA.Error)?.errorResponse

                        if (errorResponse != null) {
                            // If it's a Forbidden error in the context of reset password/UIA,
                            // it specifically means "Wrong Current Password", not general "Invalid username or password"
                            // because the user is already logged in.
                            if (errorResponse is ErrorResponse.Forbidden) {
                                Result.failure(AuthenticationException.InvalidCredentials("Access denied. Incorrect current password."))
                            } else {
                                val ex = MatrixServerException(HttpStatusCode.Unauthorized, errorResponse)
                                Result.failure(ex.mapAuthenticationException())
                            }
                        } else {
                            Logger.withTag("ProfileScreenModel").e("Reset password UIA failed or requires more steps: $authResponse")
                            Result.failure(Exception("Authentication failed during password reset"))
                        }
                    }
                }
                is UIA.Error -> {
                    Logger.withTag("ProfileScreenModel").e("Reset password UIA Error: ${result.errorResponse}")
                    val ex = MatrixServerException(HttpStatusCode.BadRequest, result.errorResponse)
                    Result.failure(ex.mapAuthenticationException())
                }
            }
        } catch (e: Exception) {
            Logger.withTag("ProfileScreenModel").e("Error resetting password: $e")
            Result.failure(e.mapAuthenticationException())
        }
    }

    @Composable
    override fun Render() {}
}
