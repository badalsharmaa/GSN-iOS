package io.getsafenow.libraries.gsn_matrix.api.exception

sealed class ClientException(message: String, val details: String?) : Exception(message) {
    class Generic(message: String, details: String?) : ClientException(message, details)
    class MatrixApi(val kind: ErrorKind, val code: String, message: String, details: String?) : ClientException(message, details)
    class Other(message: String) : ClientException(message, null)
}

fun ClientException.isNetworkError(): Boolean {
    return this is ClientException.Generic && message?.contains("error sending request for url", ignoreCase = true) == true
}
