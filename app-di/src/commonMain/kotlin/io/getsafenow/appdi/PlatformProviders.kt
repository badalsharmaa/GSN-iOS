package io.getsafenow.appdi

import io.getsafenow.libraries.di.AppScopeGsn
import io.getsafenow.libraries.kmputils.platformkmp.ContextFactory
import io.getsafenow.libraries.sessionstorage.impl.di.DatabaseDriverFactory
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

object PlatformProviders {
    @Provides
    @SingleIn(AppScopeGsn::class)
    fun provideDatabaseDriverFactory(contextFactory: ContextFactory): DatabaseDriverFactory =
        provideDbDriverFactory(contextFactory)
}

// Forward to platform actual
expect fun provideDbDriverFactory(contextFactory: ContextFactory): DatabaseDriverFactory