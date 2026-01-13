package io.getsafenow.libraries.gsn_matrix.api.auth

enum class AuthErrorCode(val value: String) {
    UNKNOWN("M_UNKNOWN"),
    USER_DEACTIVATED("M_USER_DEACTIVATED"),
    FORBIDDEN("M_FORBIDDEN")
}

// This is taken from the iOS version. It seems like currently there's no better way to extract error codes
val AuthenticationException.errorCode: AuthErrorCode
    get() {
        val message = (this as? AuthenticationException.Generic)?.message ?: return AuthErrorCode.UNKNOWN
        return enumValues<AuthErrorCode>()
            .firstOrNull { message.contains(it.value) }
            ?: AuthErrorCode.UNKNOWN
    }
