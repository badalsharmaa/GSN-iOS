package io.getsafenow.libraries.gsn_matrix.api.encryption

import io.getsafenow.libraries.gsn_matrix.api.exception.ClientException


sealed class RecoveryException(message: String) : Exception(message) {
    class SecretStorage(message: String) : RecoveryException(message)
    data object BackupExistsOnServer : RecoveryException("BackupExistsOnServer")
    data class Client(val exception: ClientException) : RecoveryException(exception.message ?: "Unknown error")
}
