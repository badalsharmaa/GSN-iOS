package com.example.getsafenowclient

/**
 * Minimal configuration data for building a Matrix client.
 * Works for both Android (Rust SDK) and iOS (MatrixRustComponent framework).
 */
data class ClientLoginData(
    val homeserverUrl: String = "https://spydefense.org/",
    val username: String,
    val password: String,
    val deviceName: String = "PlaceHolder DI",
    val deviceId: String= "123455677"
)