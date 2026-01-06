package io.getsafenow.libraries.kmputils.file

import co.touchlab.kermit.Logger
import io.getsafenow.libraries.kmputils.platformkmp.ContextFactory
import io.getsafenow.libraries.kmputils.platformkmp.PlatformFile
import io.getsafenow.libraries.kmputils.platformkmp.PlatformUri

actual fun deleteIfTemporary(context: ContextFactory, uri: PlatformUri) {
    val file = PlatformFile(uri.toStringUri())
    val cacheDir = context.cacheDir().path

    if (file.path.startsWith(cacheDir)) {
        if (!file.delete()) {
            Logger.w { "Failed to delete temporary file: ${file.path}" }
        } else {
            Logger.v { "Deleted temporary file: ${file.path}" }
        }
    } else {
        Logger.v { "Do not delete file: ${file.path}" }
    }
}
