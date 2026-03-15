package com.example.getsafenowclient.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.getsafenow.libraries.kmputils.platformkmp.ContextFactory
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

/**
 * iOS implementation of createCallStateDataStore.
 * Uses iOS documents directory for persistent storage.
 */
@OptIn(ExperimentalForeignApi::class)
actual fun createCallStateDataStore(contextFactory: ContextFactory): DataStore<Preferences> {
    return createDataStore(
        produceFile = {
            val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
                directory = NSDocumentDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = false,
                error = null
            )
            requireNotNull(documentDirectory) { "Could not access iOS documents directory" }
            "${documentDirectory.path}/call_state.preferences_pb"
        }
    )
}

/**
 * Helper function to create DataStore with file path.
 * This uses the DataStore library's standard creation method.
 */
private fun createDataStore(produceFile: () -> String): DataStore<Preferences> {
    // DataStore creation for iOS
    // The actual implementation depends on your DataStore setup
    // This is a standard pattern for iOS DataStore
    return androidx.datastore.preferences.core.PreferenceDataStoreFactory.createWithPath(
        produceFile = { produceFile() }
    )
}
