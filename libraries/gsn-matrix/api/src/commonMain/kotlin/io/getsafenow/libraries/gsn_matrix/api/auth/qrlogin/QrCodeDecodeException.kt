package io.getsafenow.libraries.gsn_matrix.api.auth.qrlogin

sealed class QrCodeDecodeException(message: String) : Exception(message) {
    class Crypto(
        message: String,
//        val reason: Reason
    ) : QrCodeDecodeException(message) {
        // We plan to restore it in the future when UniFFi can process them
//        enum class Reason {
//            NOT_ENOUGH_DATA,
//            NOT_UTF8,
//            URL_PARSE,
//            INVALID_MODE,
//            INVALID_VERSION,
//            BASE64,
//            INVALID_PREFIX
//        }
    }
}
