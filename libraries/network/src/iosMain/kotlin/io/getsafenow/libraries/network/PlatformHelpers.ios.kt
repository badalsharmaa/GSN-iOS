package io.getsafenow.libraries.network

import io.ktor.client.engine.HttpClientEngine

actual fun configureEngine(engine: HttpClientEngine) {
    // Darwin engine timeout configuration
    // Note: Darwin engine doesn't expose timeout configuration like OkHttp
    // The timeouts are handled by the underlying URLSession
    // For now, we'll use the default timeouts which are reasonable
    // This is a limitation of the Darwin engine compared to OkHttp
}