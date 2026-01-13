package io.getsafenow.libraries.sessionstorage.impl.di

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import io.getsafenow.libraries.sessionstorage.impl.database.ClientSessionDatabase
import io.getsafenow.libraries.di.ApplicationContextGsn
import me.tatarka.inject.annotations.Inject

// Inject the app Context via your existing qualifier
actual class DatabaseDriverFactory @Inject constructor(
    @ApplicationContextGsn val context: Context
)

internal actual fun createSqlDriver(
    driverFactory: DatabaseDriverFactory,
    dbName: String
): SqlDriver = AndroidSqliteDriver(
    schema = ClientSessionDatabase.Schema,
    context = driverFactory.run { context },
    name = dbName
)