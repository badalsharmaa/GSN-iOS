package io.getsafenow.libraries.network.useragent

/**
 * KMP-compatible interface for providing User-Agent strings.
 *
 * This interface provides User-Agent strings for HTTP requests
 * across different platforms (Android, iOS).
 */
interface UserAgentProvider {
    fun provide(): String
}