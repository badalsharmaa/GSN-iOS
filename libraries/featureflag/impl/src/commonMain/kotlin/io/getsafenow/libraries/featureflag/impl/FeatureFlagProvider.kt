package io.getsafenow.libraries.featureflag.impl

import io.getsafenow.libraries.featureflag.api.Feature
import kotlinx.coroutines.flow.Flow


interface FeatureFlagProvider {
    val priority: Int
    fun isFeatureEnabledFlow(feature: Feature): Flow<Boolean>
    fun hasFeature(feature: Feature): Boolean
}

const val LOW_PRIORITY = 0
const val MEDIUM_PRIORITY = 1
const val HIGH_PRIORITY = 2
