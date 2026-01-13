package io.getsafenow.libraries.featureflag.impl

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import io.getsafenow.libraries.featureflag.api.Feature
import io.getsafenow.libraries.gsn_core.meta.BuildMeta
import io.getsafenow.libraries.preferences.api.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import me.tatarka.inject.annotations.Inject


/**
 * Note: this will be used only in the nightly and in the debug build.
 */
class PreferencesFeatureFlagProvider @Inject constructor(
    private val buildMeta: BuildMeta,
    preferenceDataStoreFactory: PreferenceDataStoreFactory,
) : MutableFeatureFlagProvider {
    private val store = preferenceDataStoreFactory.create("getsafenow_featureflag")

    override val priority = MEDIUM_PRIORITY

    override suspend fun setFeatureEnabled(feature: Feature, enabled: Boolean) {
        store.edit { prefs ->
            prefs[booleanPreferencesKey(feature.key)] = enabled
        }
    }

    override fun isFeatureEnabledFlow(feature: Feature): Flow<Boolean> {
        return store.data.map { prefs ->
            prefs[booleanPreferencesKey(feature.key)] ?: feature.defaultValue(buildMeta)
        }.distinctUntilChanged()
    }

    override fun hasFeature(feature: Feature): Boolean {
        return true
    }
}
