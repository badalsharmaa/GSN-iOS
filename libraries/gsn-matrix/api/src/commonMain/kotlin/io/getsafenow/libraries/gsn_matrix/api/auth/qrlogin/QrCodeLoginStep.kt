package io.getsafenow.libraries.gsn_matrix.api.auth.qrlogin

sealed interface QrCodeLoginStep {
    data object Uninitialized : QrCodeLoginStep
    data class EstablishingSecureChannel(val checkCode: String) : QrCodeLoginStep
    data object Starting : QrCodeLoginStep
    data class WaitingForToken(val userCode: String) : QrCodeLoginStep
    data class Failed(val error: QrLoginException) : QrCodeLoginStep
    data object Finished : QrCodeLoginStep
}
