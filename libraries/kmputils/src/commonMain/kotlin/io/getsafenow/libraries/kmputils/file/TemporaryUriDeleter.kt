package io.getsafenow.libraries.kmputils.file

import io.getsafenow.libraries.di.AppScopeGsn
import io.getsafenow.libraries.di.ApplicationContextGsn
import io.getsafenow.libraries.kmputils.platformkmp.ContextFactory
import io.getsafenow.libraries.kmputils.platformkmp.PlatformUri
import me.tatarka.inject.annotations.Inject
import software.amazon.lastmile.kotlin.inject.anvil.ContributesBinding

/**
 * Deletes a URI/file only if it is temporary (cache).
 */
interface TemporaryUriDeleter {
    fun delete(uri: PlatformUri?)
}

/**
 * KMP-compatible implementation.
 */
@ContributesBinding(AppScopeGsn::class)
class DefaultTemporaryUriDeleter @Inject constructor(
    @ApplicationContextGsn private val context: ContextFactory
) : TemporaryUriDeleter {
    override fun delete(uri: PlatformUri?) {
        uri ?: return
        deleteIfTemporary(context, uri)
    }
}

/**
 * Expect function for platform-specific deletion logic.
 */
expect fun deleteIfTemporary(context: ContextFactory, uri: PlatformUri)