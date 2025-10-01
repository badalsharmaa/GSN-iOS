package io.getsafenow.libraries.gsn_matrix.api.core

interface ProgressCallback {
    fun onProgress(current: Long, total: Long)
}
