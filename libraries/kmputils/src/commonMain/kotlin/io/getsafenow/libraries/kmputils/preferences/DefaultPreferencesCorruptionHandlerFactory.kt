package io.getsafenow.libraries.kmputils.preferences

import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences


object DefaultPreferencesCorruptionHandlerFactory {
    /**
     * Creates a [ReplaceFileCorruptionHandler] that will replace the corrupted preferences file with an empty preferences object.
     */
    fun replaceWithEmpty(): ReplaceFileCorruptionHandler<Preferences> {
        return ReplaceFileCorruptionHandler(
            produceNewData = {
                // If the preferences file is corrupted, we return an empty preferences object
                emptyPreferences()
            },
        )
    }
}
