package io.getsafenow.libraries.gsn_matrix.impl.auth

import io.getsafenow.libraries.gsn_matrix.api.auth.AuthenticationException
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.io.IOException
import net.folivo.trixnity.core.ErrorResponse
import net.folivo.trixnity.core.MatrixServerException

/**
 * Centralized Matrix → AuthenticationException mapper.
 * Provides consistent, user-friendly messages for every relevant Matrix authentication error.
 * Fully covers all AuthenticationException subclasses for completeness.
 */
fun Throwable.mapAuthenticationException(): AuthenticationException {
    val message = this.message ?: "Unknown error"

    return when (this) {

        // ---- Matrix protocol / server responses ----
        is MatrixServerException -> {
            val err = this.errorResponse
            val msg = err.error.lowercase()

            // GLOBAL EMAIL FORMAT DETECTION (covers ALL errcodes)
            if (
                msg.contains("unable to parse email") ||
                (msg.contains("email") && msg.contains("parse"))
            ) {
                return AuthenticationException.InvalidServerName("Please enter a valid email address.")
            }
            // ---- continue with when(err) ----
            when (err) {

                // ❌ Invalid credentials or authorization problems
                is ErrorResponse.Forbidden ->
                    AuthenticationException.InvalidCredentials("Access denied. Invalid username or password.")

                is ErrorResponse.UnknownToken ->
                    AuthenticationException.InvalidCredentials("Your session has expired. Please log in again.")

                is ErrorResponse.MissingToken ->
                    AuthenticationException.InvalidCredentials("Missing authentication token. Please sign in.")

                is ErrorResponse.Unauthorized ->
                    AuthenticationException.InvalidCredentials("Unauthorized access. Please check your credentials.")

                // ⚙️ Malformed or invalid server / request issues
                is ErrorResponse.InvalidParam ->
                    AuthenticationException.InvalidServerName("Invalid parameter in request.")

                is ErrorResponse.MissingParam ->
                    AuthenticationException.InvalidServerName("Required field missing from request.")

                is ErrorResponse.BadJson ->
                    AuthenticationException.InvalidServerName("Invalid JSON structure received from server.")

                is ErrorResponse.NotJson ->
                    AuthenticationException.InvalidServerName("Response was not valid JSON. Possible server misconfiguration.")

                // 📧 Email / 3PID already exists or domain restricted
                is ErrorResponse.UserInUse ->
                    AuthenticationException.EmailExists("An account with this username already exists.")

                is ErrorResponse.ThirdPIdInUse ->
                    AuthenticationException.EmailExists("An account with this email already exists.")

                is ErrorResponse.ThirdPIdDenied ->
                    AuthenticationException.EmailExists("Email domain not permitted for registration.")

                is ErrorResponse.ThirdPIdAuthFailed ->
                    AuthenticationException.EmailExists("Email verification failed during registration.")

                // 🚫 Account restrictions
                is ErrorResponse.UserLocked ->
                    AuthenticationException.UserRestricted("Your account is locked. Please contact support.")

                is ErrorResponse.UserSuspended ->
                    AuthenticationException.UserRestricted("Your account has been suspended temporarily.")

                is ErrorResponse.UserDeactivated ->
                    AuthenticationException.UserRestricted("Your account has been deactivated and cannot be used.")

                // 👤 Invalid username or not found
                is ErrorResponse.InvalidUsername ->
                    AuthenticationException.InvalidCredentials("The username format is invalid.")

                is ErrorResponse.ThirdPIdNotFound ->
                    AuthenticationException.InvalidCredentials("No account found with this email address.")

                // 🧾 Captcha or throttling
                is ErrorResponse.CaptchaNeeded ->
                    AuthenticationException.Generic("Captcha verification required to continue.")

                is ErrorResponse.CaptchaInvalid ->
                    AuthenticationException.Generic("Captcha verification failed. Please try again.")

                is ErrorResponse.LimitExceeded ->
                    AuthenticationException.NetworkError("Too many attempts. Please wait a few seconds and try again.")

                // 🌀 Sliding sync or protocol issues
                is ErrorResponse.Unknown -> {
                    if (err.error.contains("SLIDING_SYNC", ignoreCase = true)) {
                        AuthenticationException.SlidingSyncVersion("Incompatible app version. Please update your app.")
                    } else {
                        AuthenticationException.Generic("Unexpected server error: ${err.error}")
                    }
                }

                // 🔑 OIDC / SSO related errors
                is ErrorResponse.ServerNotTrusted ->
                    AuthenticationException.Oidc("The server is not trusted or OIDC login failed.")

                // ✉️ Verification pending (manual continuation required)
                is ErrorResponse.ThirdPartyMediumNotSupported ->
                    AuthenticationException.VerificationPending("Email verification pending. Please check your inbox to continue registration.")

                // 💾 Server quota or internal limits
                is ErrorResponse.ResourceLimitExceeded ->
                    AuthenticationException.Generic("The server has reached its capacity limit. Try again later.")

                // Default fallback for any other Matrix error type
                else -> AuthenticationException.Generic("Matrix server error: ${err.error}")
            }
        }

        // 🌐 Network or connection failures
        is IOException ->
            AuthenticationException.NetworkError("No internet connection. Please check your network.")
        is ConnectTimeoutException ->
            AuthenticationException.NetworkError("Connection timed out. Server may be unreachable.")
        is HttpRequestTimeoutException ->
            AuthenticationException.NetworkError("Server took too long to respond. Please try again.")

        // 🧩 Any other unhandled error
        else -> AuthenticationException.Generic("Unexpected error occurred: $message")
    }

}
