package io.getsafenow.libraries.featureflag.impl

import io.getsafenow.libraries.di.AppScopeGsn
import io.getsafenow.libraries.featureflag.api.Feature
import io.getsafenow.libraries.featureflag.api.FeatureFlagService
import io.getsafenow.libraries.gsn_core.meta.BuildMeta
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import me.tatarka.inject.annotations.Inject
import software.amazon.lastmile.kotlin.inject.anvil.ContributesBinding
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn
import kotlin.jvm.JvmSuppressWildcards


@ContributesBinding(AppScopeGsn::class)
@SingleIn(AppScopeGsn::class)
class DefaultFeatureFlagService @Inject constructor(
    private val providers: Set<@JvmSuppressWildcards FeatureFlagProvider>,
    private val buildMeta: BuildMeta,
) : FeatureFlagService {
    override fun isFeatureEnabledFlow(feature: Feature): Flow<Boolean> {
        return providers.filter { it.hasFeature(feature) }
            .maxByOrNull(FeatureFlagProvider::priority)
            ?.isFeatureEnabledFlow(feature)
            ?: flowOf(feature.defaultValue(buildMeta))
    }

    override suspend fun setFeatureEnabled(feature: Feature, enabled: Boolean): Boolean {
        return providers.filterIsInstance<MutableFeatureFlagProvider>()
            .minByOrNull(FeatureFlagProvider::priority)
            ?.setFeatureEnabled(feature, enabled)
            ?.let { true }
            ?: false
    }
}
