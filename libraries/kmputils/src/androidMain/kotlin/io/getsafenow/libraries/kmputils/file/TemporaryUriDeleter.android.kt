package io.getsafenow.libraries.kmputils.file

import android.net.Uri
import co.touchlab.kermit.Logger
import io.getsafenow.libraries.kmputils.platformkmp.ContextFactory
import io.getsafenow.libraries.kmputils.platformkmp.PlatformUri

actual fun deleteIfTemporary(context: ContextFactory, uri: PlatformUri) {
    try {
        val androidContext = context.getContextAs<android.content.Context>()
        val baseCacheUri = "content://${androidContext.packageName}.fileprovider/cache"

        if (uri.raw.startsWith(baseCacheUri)) {
            val androidUri = Uri.parse(uri.raw)
            androidContext.contentResolver.delete(androidUri, null, null)
            Logger.v { "Deleted temporary URI: ${uri.raw}" }
        } else {
            Logger.v { "Do not delete URI: ${uri.raw}" }
        }
    } catch (e: Exception) {
        Logger.e(message = { "Failed to delete URI: ${uri.raw}" }, throwable = e)
    }
}

// Helper to cast context safely
private inline fun <reified T> ContextFactory.getContextAs(): T =
    getContext() as? T ?: throw IllegalStateException("Context is not of type ${T::class}")
