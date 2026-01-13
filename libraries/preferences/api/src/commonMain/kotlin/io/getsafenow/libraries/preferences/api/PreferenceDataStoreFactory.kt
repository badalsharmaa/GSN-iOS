package io.getsafenow.libraries.preferences.api

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

/**
 * Factory used to create a [DataStore] for preferences.
 *
 * It's a wrapper around AndroidX's `PreferenceDataStoreFactory` to make testing easier.
 */
interface PreferenceDataStoreFactory {
    fun create(name: String): DataStore<Preferences>
}
