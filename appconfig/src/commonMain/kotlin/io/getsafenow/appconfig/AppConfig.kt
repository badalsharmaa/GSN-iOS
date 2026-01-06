package io.getsafenow.appconfig

/**
 * Interface for accessing application configuration values.
 * This provides a clean API for accessing BuildKonfig values.
 */
interface AppConfig {
    // Build Configuration
    val isEnterpriseBuild: Boolean

    // Server Configuration
    val defaultHomeserver: String
    val slidingSyncProxy: String
    val oidcEnabled: Boolean
    val analyticsEnabled: Boolean

    // App Configuration
    val clientAppName: String
    val appName: String
    val appVersion: String
    val deviceNameFormat: String

    // Policy URLs
    val urlPolicy: String
    val bugReportUrl: String
    val bugReportAppName: String

    // Analytics Configuration
    val posthogHost: String
    val posthogApiKey: String
    val sentryDsn: String

    // Push Configuration
    val pushIncludeFirebase: Boolean
    val pushIncludeUnifiedPush: Boolean

    // OIDC Configuration
    val clientUri: String
    val logoUri: String
    val tosUri: String
    val policyUri: String

    val debug: Boolean
    val defaultUser: String
    val defaultPwd: String
}