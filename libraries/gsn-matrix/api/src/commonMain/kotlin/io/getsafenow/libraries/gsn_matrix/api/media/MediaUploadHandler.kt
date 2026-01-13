package io.getsafenow.libraries.gsn_matrix.api.media

/**
 * This is an abstraction over the Rust SDK's `SendAttachmentJoinHandle` which allows us to either [await] the upload process or [cancel] it.
 */
interface MediaUploadHandler {
    /** Await the upload process to finish. */
    suspend fun await(): Result<Unit>

    /** Cancel the upload process. */
    fun cancel()
}
