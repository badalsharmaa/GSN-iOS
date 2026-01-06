package io.getsafenow.appdi

import io.getsafenow.libraries.kmputils.platformkmp.ContextFactory
import io.getsafenow.libraries.sessionstorage.impl.di.DatabaseDriverFactory

actual fun provideDbDriverFactory(contextFactory: ContextFactory): DatabaseDriverFactory {
    return DatabaseDriverFactory()
}