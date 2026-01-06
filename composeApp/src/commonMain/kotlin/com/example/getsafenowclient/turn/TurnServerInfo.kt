package com.example.getsafenowclient.turn

data class TurnServerInfo(
    val username: String,
    val password: String,
    val uris: List<String>,
    val expiresAtMs: Long
)