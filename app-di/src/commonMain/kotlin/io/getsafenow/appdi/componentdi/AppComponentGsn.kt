package io.getsafenow.appdi.componentdi

import io.getsafenow.libraries.di.AppScopeGsn
import io.getsafenow.libraries.gsn_matrix.api.GsnMClient
import io.getsafenow.services.toolkit.api.intent.ExternalIntentLauncher
import io.getsafenow.services.toolkit.api.sdk.BuildVersionSdkIntProvider
import io.getsafenow.services.toolkit.api.systemclock.SystemClock
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import kotlin.time.TimeSource

/**
 * Root DI component for the whole app.
 * Provides application-scoped dependencies.
 */

@AppScopeGsn
@Component
abstract class AppComponentGsn {
    abstract val externalIntentLauncher: ExternalIntentLauncher
    abstract val buildVersionSdkIntProvider: BuildVersionSdkIntProvider
    abstract val systemClock: SystemClock
    abstract val timeSource: kotlin.time.TimeSource
    abstract fun newSessionComponent(client: GsnMClient): SessionComponentGsn


    //providers-----
    @Provides
    @AppScopeGsn
    fun provideTimeSource(): TimeSource = TimeSource.Monotonic

/*    // In AppComponentGsn (DI root), expose the bindings:
    @Provides
    fun provideComponentFactories(bindings: AppComponentFactories): ComponentFactoriesBindings = bindings*/
}