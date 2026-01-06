package io.getsafenow.libraries.sessionstorage.impl.di

//import io.getsafenow.libraries.sessionstorage.impl.GsnDatabaseSessionStore
import app.cash.sqldelight.db.SqlDriver
import io.getsafenow.libraries.di.AppScopeGsn
import io.getsafenow.libraries.sessionstorage.impl.database.ClientSessionDatabase
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

// Platform driver factory
expect class DatabaseDriverFactory

// Module holding simple config (DB name)
class ClientSessionStorageModule(
    val dbName: String = "client_session.db"
)

// Provider functions (scope to AppScopeGsn)
@Provides
@SingleIn(AppScopeGsn::class)
fun provideSqlDriver(
    driverFactory: DatabaseDriverFactory,
    module: ClientSessionStorageModule
): SqlDriver = createSqlDriver(driverFactory, module.dbName)

@Provides
@SingleIn(AppScopeGsn::class)
fun provideClientSessionDatabase(driver: SqlDriver): ClientSessionDatabase =
    ClientSessionDatabase(driver)

/*@Provides
@AppScopeGsn
fun provideClientSessionStore(db: ClientSessionDatabase): ClientSessionStore =
    GsnDatabaseSessionStore(db)*/

// Small helper to keep commonMain clean
internal expect fun createSqlDriver(
    driverFactory: DatabaseDriverFactory,
    dbName: String
): SqlDriver