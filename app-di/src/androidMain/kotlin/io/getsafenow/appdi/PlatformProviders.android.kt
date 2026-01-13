package io.getsafenow.appdi

import android.content.Context
import io.getsafenow.libraries.kmputils.platformkmp.ContextFactory

import io.getsafenow.libraries.sessionstorage.impl.di.DatabaseDriverFactory

actual fun provideDbDriverFactory(contextFactory: ContextFactory): DatabaseDriverFactory {
    val app = contextFactory.getApplication() as Context
    return DatabaseDriverFactory(app)
}