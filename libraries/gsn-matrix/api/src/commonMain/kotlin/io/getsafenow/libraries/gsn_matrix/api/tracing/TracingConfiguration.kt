package io.getsafenow.libraries.gsn_matrix.api.tracing

data class TracingConfiguration(
    val logLevel: LogLevel,
    val extraTargets: List<String>,
    val traceLogPacks: Set<TraceLogPack>,
    val writesToLogcat: Boolean,
    val writesToFilesConfiguration: WriteToFilesConfiguration,
)
