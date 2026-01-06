package io.getsafenow.libraries.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttpConfig
import java.util.concurrent.TimeUnit


actual fun configureEngine(engine: HttpClientEngine) {
    if (engine is OkHttpConfig) {
        engine.config {
            connectTimeout(30, TimeUnit.SECONDS)
            readTimeout(60, TimeUnit.SECONDS)
            writeTimeout(60, TimeUnit.SECONDS)
        }
    }
}