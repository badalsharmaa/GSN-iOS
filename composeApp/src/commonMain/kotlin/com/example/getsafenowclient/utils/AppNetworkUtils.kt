package com.example.getsafenowclient.utils

import co.touchlab.kermit.Logger
import io.getsafenow.libraries.gsn_matrix.impl.auth.mapAuthenticationException
import net.folivo.trixnity.core.MatrixServerException

suspend fun <T> safeAuthCall(action: suspend () -> T): T {
    return try {
        action()
    } catch (e: Exception) {
        if (e is MatrixServerException) {
            Logger.e { "Matrix error type=${e.errorResponse::class.simpleName}, msg='${e.errorResponse.error}'" }
        }
        throw e.mapAuthenticationException()
    }
}