package io.getsafenow.libraries.gsn_matrix.api.auth.qrlogin

interface MQrCodeLoginDataFactory {
    fun parseQrCodeData(data: ByteArray): Result<MQrCodeLoginData>
}
