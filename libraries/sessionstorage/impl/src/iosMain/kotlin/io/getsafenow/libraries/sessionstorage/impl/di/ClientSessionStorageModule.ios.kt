package io.getsafenow.libraries.sessionstorage.impl.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import io.getsafenow.libraries.sessionstorage.impl.database.ClientSessionDatabase
import me.tatarka.inject.annotations.Inject

actual class DatabaseDriverFactory @Inject constructor()

internal actual fun createSqlDriver(
    driverFactory: DatabaseDriverFactory,
    dbName: String
): SqlDriver = NativeSqliteDriver(
    schema = ClientSessionDatabase.Schema,
    name = dbName
)