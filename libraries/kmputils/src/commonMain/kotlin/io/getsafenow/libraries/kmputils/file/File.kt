package io.getsafenow.libraries.kmputils.file

import co.touchlab.kermit.Logger
import io.getsafenow.libraries.gsn_core.gsndata.runCatchingOrNull
import io.getsafenow.libraries.kmputils.platformkmp.ContextFactory
import io.getsafenow.libraries.kmputils.platformkmp.PlatformFile
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// ---------------- Safe file operations ----------------

fun PlatformFile.safeDelete() {
    if (!exists()) return
    runCatchingOrNull(
        onException = { Logger.e(message = { "Error, unable to delete file $path" }, throwable = it) },
        operation = {
            if (!delete()) Logger.w("Warning, unable to delete file $path")
        }
    )
}

fun PlatformFile.safeRenameTo(dest: PlatformFile) {
    runCatchingOrNull(
        onException = { Logger.e(message = { "Error, unable to rename file $path to ${dest.path}" }, throwable = it) },
        operation = {
            if (!renameTo(dest)) Logger.w("Warning, unable to rename file $path to ${dest.path}")
        }
    )
}

// ---------------- Temporary file creation ----------------

@OptIn(ExperimentalUuidApi::class)
fun ContextFactory.createTmpFile(baseDir: PlatformFile? = null, extension: String? = null): PlatformFile {
    val dir = baseDir ?: cacheDir() // get default platform cache directory
    val suffix = extension?.let { ".$it" }
    return PlatformFile.createTempFile(Uuid.random().toString(), suffix, dir).apply { mkdirs() }
}

// ---------------- File size calculation ----------------

suspend fun PlatformFile.getSizeOfFiles(): Long = withContext(Dispatchers.Default) {
    var total = 0L
    for (file in walkTopDown()) {
        Logger.v { "Get size of ${file.path}" }
        total += file.length()
    }
    total
}
