package io.getsafenow.libraries.gsn_matrix.api.core

fun interface SendHandle {
    suspend fun retry(): Result<Unit>
}
