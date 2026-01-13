package io.getsafenow.libraries.featureflag.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first


interface FeatureFlagService {
    /**
     * @param feature the feature to check for
     *
     * @return true if the feature is enabled
     */
    suspend fun isFeatureEnabled(feature: Feature): Boolean = isFeatureEnabledFlow(feature).first()

    /**
     * @param feature the feature to check for
     *
     * @return a flow of booleans, true if the feature is enabled, false if it is disabled.
     */
    fun isFeatureEnabledFlow(feature: Feature): Flow<Boolean>

    /**
     * @param feature the feature to enable or disable
     * @param enabled true to enable the feature
     *
     * @return true if the method succeeds, ie if a [io.getsafenow.libraries.featureflag.impl.MutableFeatureFlagProvider]
     * is registered
     */
    suspend fun setFeatureEnabled(feature: Feature, enabled: Boolean): Boolean
}
