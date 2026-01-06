package io.getsafenow.libraries.gsn_matrix.api.auth

sealed class AuthenticationException(message: String) : Exception(message) {
    class InvalidServerName(message: String) : AuthenticationException(message)
    class InvalidCredentials(message: String) : AuthenticationException(message)
    class EmailExists(message: String) : AuthenticationException(message)
    class NetworkError(message: String) : AuthenticationException(message)
    class VerificationPending(message: String) : AuthenticationException(message)
    class UserRestricted(message: String) : AuthenticationException(message)
    class SlidingSyncVersion(message: String) : AuthenticationException(message)
    class Oidc(message: String) : AuthenticationException(message)
    class Generic(message: String) : AuthenticationException(message)
}