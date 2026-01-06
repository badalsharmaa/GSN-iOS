package io.getsafenow.libraries.network.headers

/**
 * HTTP header constants used across the network module.
 *
 * These headers are used for:
 * - Authentication (Authorization)
 * - User agent identification (User-Agent)
 * - Content type negotiation
 * - Matrix-specific headers
 */
@Suppress("ktlint:standard:property-naming")
object HttpHeaders {
    // Standard HTTP headers
    const val Authorization = "Authorization"
    const val UserAgent = "User-Agent"
    const val ContentType = "Content-Type"
    const val Accept = "Accept"
    const val ContentLength = "Content-Length"

    // Matrix-specific headers
    const val MatrixServerName = "X-Matrix-Server-Name"
    const val MatrixAccessToken = "X-Matrix-Access-Token"
    const val MatrixDeviceId = "X-Matrix-Device-Id"

    // CORS and security headers
    const val Origin = "Origin"
    const val Referer = "Referer"
    const val XRequestedWith = "X-Requested-With"

    // Cache control headers
    const val CacheControl = "Cache-Control"
    const val ETag = "ETag"
    const val IfNoneMatch = "If-None-Match"
    const val IfModifiedSince = "If-Modified-Since"

    // Connection headers
    const val Connection = "Connection"
    const val KeepAlive = "Keep-Alive"

    // Error handling headers
    const val RetryAfter = "Retry-After"
    const val XRateLimitRemaining = "X-Rate-Limit-Remaining"
    const val XRateLimitReset = "X-Rate-Limit-Reset"
}
