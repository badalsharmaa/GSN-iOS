package io.getsafenow.libraries.featureflag.impl.di

import io.getsafenow.libraries.di.AppScopeGsn
import io.getsafenow.libraries.featureflag.impl.FeatureFlagProvider
import io.getsafenow.libraries.featureflag.impl.PreferencesFeatureFlagProvider
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.ContributesTo

@Component
@ContributesTo(AppScopeGsn::class)
object FeatureFlagModule {
    @Provides
    fun providesFeatureFlagProvider(
        mutableFeatureFlagProvider: PreferencesFeatureFlagProvider,
    ): Set<FeatureFlagProvider> {
        return buildSet {
            add(mutableFeatureFlagProvider)
        }
    }
}
