package io.getsafenow.libraries.sessionstorage.impl

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import io.getsafenow.libraries.sessionstorage.api.ClientSessionData
import io.getsafenow.libraries.sessionstorage.api.ClientSessionStore
import io.getsafenow.libraries.sessionstorage.api.LoggedInState
import io.getsafenow.libraries.sessionstorage.impl.database.ClientSessionDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.CoroutineContext
import kotlin.time.ExperimentalTime

class GsnDatabaseSessionStore(
    private val database: ClientSessionDatabase,
    private val queryContext: CoroutineContext = Dispatchers.Default,
) : ClientSessionStore {
    private val sessionDataMutex = Mutex()

    override fun isLoggedIn(): Flow<LoggedInState> {
        return database.clientSessionDataQueries.selectFirst()
            .asFlow()
            .mapToOneOrNull(queryContext)
            .map { row ->
                if (row == null) LoggedInState.NotLoggedIn
                else LoggedInState.LoggedIn(
                    clientSessionId = row.clientId,
                    isTokenValid = row.isTokenValid == 1L
                )
            }
    }

    override fun sessionsFlow(): Flow<List<ClientSessionData>> {
        return database.clientSessionDataQueries.selectAll()
            .asFlow()
            .mapToList(queryContext)
            .map { rows -> rows.map { it.toApiModel() } }
    }

    override suspend fun storeData(sessionData: ClientSessionData) {
        sessionDataMutex.withLock {
            database.clientSessionDataQueries.insertSessionData(sessionData.toDbModel())
        }
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun updateData(sessionData: ClientSessionData) {
        sessionDataMutex.withLock {
            val existing = database.clientSessionDataQueries
                .selectByUserId(sessionData.clientId)
                .executeAsOneOrNull()
                ?.toApiModel()
                ?: return

            // Keep previous loginTimestamp, update the rest
            database.clientSessionDataQueries.updateSession(
                sessionData.copy(
                    loginTimestamp = existing.loginTimestamp,
                ).toDbModel()
            )
        }
    }

    override suspend fun getSession(sessionId: String): ClientSessionData? {
        return sessionDataMutex.withLock {
            database.clientSessionDataQueries
                .selectByUserId(sessionId)
                .executeAsOneOrNull()
                ?.toApiModel()
        }
    }

    override suspend fun getAllSessions(): List<ClientSessionData> {
        return sessionDataMutex.withLock {
            database.clientSessionDataQueries
                .selectAll()
                .executeAsList()
                .map { it.toApiModel() }
        }
    }

    override suspend fun getLatestSession(): ClientSessionData? {
        return sessionDataMutex.withLock {
            database.clientSessionDataQueries
                .selectFirst()
                .executeAsOneOrNull()
                ?.toApiModel()
        }
    }

    override suspend fun removeSession(sessionId: String) {
        sessionDataMutex.withLock {
            database.clientSessionDataQueries.removeSession(sessionId)
        }
    }
}