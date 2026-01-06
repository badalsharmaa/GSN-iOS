package io.getsafenow.libraries.preferences.impl

import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.Preferences

class SessionPreferencesStoreMigration(
    private val sharePresenceKey: Preferences.Key<Boolean>,
    private val sendPublicReadReceiptsKey: Preferences.Key<Boolean>,
) : DataMigration<Preferences> {
    override suspend fun cleanUp() = Unit

    override suspend fun shouldMigrate(currentData: Preferences): Boolean {
        return currentData[sharePresenceKey] == null
    }

    override suspend fun migrate(currentData: Preferences): Preferences {
        // If sendPublicReadReceiptsKey was false, consider that sharing presence is false.
        val defaultValue = currentData[sendPublicReadReceiptsKey] ?: true
        return currentData.toMutablePreferences().apply {
            set(sharePresenceKey, defaultValue)
        }.toPreferences()
    }
}
