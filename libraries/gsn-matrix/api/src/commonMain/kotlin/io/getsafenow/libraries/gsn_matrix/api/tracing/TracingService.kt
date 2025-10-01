package io.getsafenow.libraries.gsn_matrix.api.tracing

import co.touchlab.kermit.Logger

interface TracingService {
    fun createTimberTree(target: String): Logger

    fun updateWriteToFilesConfiguration(config: WriteToFilesConfiguration)
}
