package io.getsafenow.appconfig


/**
 * Implementation of AppConfig that wraps BuildKonfig.
 * This provides access to all BuildKonfig values through a clean interface.
 */
class AppConfigImpl : AppConfig {
    // Build Configuration
    override val isEnterpriseBuild: Boolean = BuildKonfig.IS_ENTERPRISE_BUILD

    // Server Configuration
    override val defaultHomeserver: String = BuildKonfig.DEFAULT_HOMESERVER
    override val slidingSyncProxy: String = BuildKonfig.SLIDING_SYNC_PROXY
    override val oidcEnabled: Boolean = BuildKonfig.OIDC_ENABLED
    override val analyticsEnabled: Boolean = BuildKonfig.ANALYTICS_ENABLED

    // App Configuration
    override val clientAppName: String = BuildKonfig.CLIENT_APP_NAME
    override val appName: String = BuildKonfig.APP_NAME
    override val appVersion: String = BuildKonfig.APP_VERSION
    override val deviceNameFormat: String = BuildKonfig.DEVICE_NAME_FORMAT

    // Policy URLs
    override val urlPolicy: String = BuildKonfig.URL_POLICY
    override val bugReportUrl: String = BuildKonfig.BUG_REPORT_URL
    override val bugReportAppName: String = BuildKonfig.BUG_REPORT_APP_NAME

    // Analytics Configuration
    override val posthogHost: String = BuildKonfig.POSTHOG_HOST
    override val posthogApiKey: String = BuildKonfig.POSTHOG_APIKEY
    override val sentryDsn: String = BuildKonfig.SENTRY_DSN

    // Push Configuration
    override val pushIncludeFirebase: Boolean = BuildKonfig.PUSH_INCLUDE_FIREBASE
    override val pushIncludeUnifiedPush: Boolean = BuildKonfig.PUSH_INCLUDE_UNIFIED_PUSH

    // OIDC Configuration
    override val clientUri: String = BuildKonfig.CLIENT_URI
    override val logoUri: String = BuildKonfig.LOGO_URI
    override val tosUri: String = BuildKonfig.TOS_URI
    override val policyUri: String = BuildKonfig.POLICY_URI

    override val debug: Boolean = BuildKonfig.DEBUG
    override val defaultUser: String = BuildKonfig.DEFAULT_USER
    override val defaultPwd: String = BuildKonfig.DEFAULT_PWD
}