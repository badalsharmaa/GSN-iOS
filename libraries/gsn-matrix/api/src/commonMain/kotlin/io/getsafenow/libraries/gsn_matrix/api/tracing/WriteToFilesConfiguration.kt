package io.getsafenow.libraries.gsn_matrix.api.tracing

sealed interface WriteToFilesConfiguration {
    data object Disabled : WriteToFilesConfiguration
    data class Enabled(
        val directory: String,
        val filenamePrefix: String,
        val numberOfFiles: Int?,
    ) : WriteToFilesConfiguration {
        // DO NOT CHANGE: suffix *MUST* be "log" for the rageshake server to not rename the file to something generic
        val filenameSuffix = "log"
    }
}
