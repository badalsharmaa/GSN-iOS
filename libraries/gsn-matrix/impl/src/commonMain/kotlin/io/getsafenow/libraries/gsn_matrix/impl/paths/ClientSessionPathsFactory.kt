package io.getsafenow.libraries.gsn_matrix.impl.paths

import io.getsafenow.libraries.di.CacheDirectoryGsn
import io.getsafenow.libraries.kmputils.platformkmp.PlatformFile
import me.tatarka.inject.annotations.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


class ClientSessionPathsFactory @Inject constructor(
    private val baseDirectory: PlatformFile,
    @CacheDirectoryGsn private val cacheDirectory: PlatformFile,
) {
    @OptIn(ExperimentalUuidApi::class)
    fun create(): ClientSessionPaths {
        val subPath = Uuid.random().toString()
        return ClientSessionPaths(
            fileDirectory = PlatformFile(baseDirectory, subPath),
            cacheDirectory = PlatformFile(cacheDirectory, subPath),
        )
    }
}
