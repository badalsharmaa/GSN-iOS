package io.getsafenow.libraries.gsn_matrix.impl.paths

import io.getsafenow.libraries.kmputils.platformkmp.PlatformFile
import io.getsafenow.libraries.sessionstorage.api.ClientSessionData


data class ClientSessionPaths(
    val fileDirectory: PlatformFile,
    val cacheDirectory: PlatformFile,
) {
    fun deleteRecursively() {
        fileDirectory.deleteRecursively()
        cacheDirectory.deleteRecursively()
    }
}

internal fun ClientSessionData.getSessionPaths(): ClientSessionPaths {
    return ClientSessionPaths(
        fileDirectory = PlatformFile(clientSessionPath),
        cacheDirectory = PlatformFile(cachePath),
    )
}
