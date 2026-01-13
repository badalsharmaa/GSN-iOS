package io.getsafenow.libraries.sessionstorage.impl

import io.getsafenow.libraries.sessionstorage.api.ClientSessionData
import io.getsafenow.libraries.sessionstorage.api.GsnLoginType
import kotlinx.datetime.Instant
import kotlin.time.ExperimentalTime
import io.getsafenow.libraries.sessionstorage.impl.database.ClientSessionData as DbClientSessionData



@OptIn(ExperimentalTime::class)
internal fun ClientSessionData.toDbModel(): DbClientSessionData {
    return DbClientSessionData(
        clientId = clientId,
        deviceId = deviceId,
        accessToken = accessToken,
        refreshToken = refreshToken,
        clientServerUrl = clientServerUrl,
        slidingSyncProxy = slidingSyncProxy,
        loginTimestamp = loginTimestamp?.toEpochMilliseconds(),
        oidcData = oidcData,
        isTokenValid = if (isTokenValid) 1L else 0L,
        gsnLoginType = gsnLoginType.name,
        securityPhase = securityPhase,
        clientSessionPath = clientSessionPath,
        cachePath = cachePath,
    )
}

@OptIn(ExperimentalTime::class)
internal fun DbClientSessionData.toApiModel(): ClientSessionData {
    return ClientSessionData(
        clientId = clientId,
        deviceId = deviceId,
        accessToken = accessToken,
        refreshToken = refreshToken,
        clientServerUrl = clientServerUrl,
        slidingSyncProxy = slidingSyncProxy,
        loginTimestamp = loginTimestamp?.let { millis ->
            val secs = millis / 1000
            val nanos = ((millis % 1000) * 1_000_000).toInt()
            Instant.fromEpochSeconds(secs, nanos)
        },
        oidcData = oidcData,
        isTokenValid = isTokenValid == 1L,
        gsnLoginType = GsnLoginType.fromName(gsnLoginType ?: GsnLoginType.UNKNOWN.name),
        securityPhase = securityPhase,
        clientSessionPath = clientSessionPath,
        cachePath = cachePath,
    )
}