package io.getsafenow.libraries.network.interceptors


import io.getsafenow.libraries.network.headers.HttpHeaders
import io.getsafenow.libraries.network.useragent.UserAgentProvider
import io.ktor.client.plugins.api.*
import io.ktor.client.request.*
import io.ktor.http.*
import me.tatarka.inject.annotations.Inject

/**
 * KMP-compatible User-Agent interceptor for Ktor HTTP client.
 *
 * This interceptor adds User-Agent header to all HTTP requests.
 * Equivalent to Element X UserAgentInterceptor but for Ktor.
 */
class UserAgentInterceptor @Inject constructor(
    private val userAgentProvider: UserAgentProvider,
) {
    val plugin = createClientPlugin("UserAgentInterceptor") {
        onRequest { request, _ ->
            val userAgent = userAgentProvider.provide()
            request.headers {
                append(HttpHeaders.UserAgent, userAgent)
            }
        }
    }
}