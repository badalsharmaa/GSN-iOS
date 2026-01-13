package io.getsafenow.libraries.network.useragent

/**
 * KMP-compatible simple User-Agent provider.
 *
 * This provider returns a static User-Agent string.
 * Useful for testing or when a simple User-Agent is sufficient.
 */
class SimpleUserAgentProvider(
    private val userAgent: String = "User agent"
) : UserAgentProvider {
    override fun provide(): String = userAgent
}