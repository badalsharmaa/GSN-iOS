package com.example.getsafenowclient.turn

import net.folivo.trixnity.clientserverapi.client.MatrixClientServerApiBaseClient

class VoipApiClientImpl(
    private val baseClient: MatrixClientServerApiBaseClient
) : VoipApiClient {

    override suspend fun getTurnServer(): GetTurnServer.Response {
        return baseClient
            .request(GetTurnServer, Unit)
            .getOrThrow()
    }
}