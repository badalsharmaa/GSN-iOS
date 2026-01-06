package io.getsafenow.libraries.network

import io.getsafenow.libraries.gsn_core.uri.ensureTrailingSlash
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Inject

/**
 * KMP-compatible factory for creating HTTP clients with JSON serialization.
 *
 * Equivalent to Element X RetrofitFactory but for Ktor HTTP client.
 */
class KtorFactory @Inject constructor(
    private val httpClientEngine: HttpClientEngine,
    private val json: Json,
) {

    /**
     * Creates a configured HttpClient for the given base URL.
     *
     * @param baseUrl The base URL for the API
     * @return Configured HttpClient with JSON serialization
     */
    fun create(baseUrl: String): HttpClient = HttpClient(httpClientEngine) {
        // Ensure trailing slash like Element X
        val normalizedUrl = baseUrl.ensureTrailingSlash()

        // Content negotiation for JSON serialization
        install(ContentNegotiation) {
            json(json)
        }

        // Set base URL for relative requests
        defaultRequest {
            url(normalizedUrl)
        }

        // Do not throw exceptions on non-2xx responses
        expectSuccess = false
    }
}