package io.getsafenow.libraries.featureflag.impl

import io.getsafenow.libraries.featureflag.api.Feature


interface MutableFeatureFlagProvider : FeatureFlagProvider {
    suspend fun setFeatureEnabled(feature: Feature, enabled: Boolean)
}
