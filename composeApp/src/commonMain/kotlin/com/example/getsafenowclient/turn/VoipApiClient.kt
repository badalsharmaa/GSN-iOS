package com.example.getsafenowclient.turn


interface VoipApiClient {
    suspend fun getTurnServer(): GetTurnServer.Response
}