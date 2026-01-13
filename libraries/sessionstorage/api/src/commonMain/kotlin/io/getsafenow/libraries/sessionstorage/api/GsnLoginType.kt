package io.getsafenow.libraries.sessionstorage.api

// Imported from Element Android, to be able to migrate from EA to EXA.
enum class GsnLoginType {
    PASSWORD,
    OIDC,
    SSO,
    UNSUPPORTED,
    CUSTOM,
    DIRECT,
    UNKNOWN,
    QR;

    companion object {
        fun fromName(name: String) = when (name) {
            PASSWORD.name -> PASSWORD
            OIDC.name -> OIDC
            SSO.name -> SSO
            UNSUPPORTED.name -> UNSUPPORTED
            CUSTOM.name -> CUSTOM
            DIRECT.name -> DIRECT
            QR.name -> QR
            else -> UNKNOWN
        }
    }
}
