package io.getsafenow.libraries.gsn_matrix.api.auth.qrlogin

sealed class QrLoginException : Exception() {
    data object Cancelled : QrLoginException()
    data object ConnectionInsecure : QrLoginException()
    data object Declined : QrLoginException()
    data object Expired : QrLoginException()
    data object LinkingNotSupported : QrLoginException()
    data object OidcMetadataInvalid : QrLoginException()
    data object SlidingSyncNotAvailable : QrLoginException()
    data object OtherDeviceNotSignedIn : QrLoginException()
    data object Unknown : QrLoginException()
}
