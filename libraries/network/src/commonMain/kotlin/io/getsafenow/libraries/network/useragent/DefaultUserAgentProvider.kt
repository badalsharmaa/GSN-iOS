package io.getsafenow.libraries.network.useragent


import io.getsafenow.libraries.gsn_core.meta.BuildMeta
import io.getsafenow.libraries.di.AppScopeGsn
import io.getsafenow.libraries.gsn_matrix.api.SdkMetadata
import me.tatarka.inject.annotations.Inject
import software.amazon.lastmile.kotlin.inject.anvil.ContributesBinding
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn


@SingleIn(AppScopeGsn::class)
@ContributesBinding(AppScopeGsn::class)
class DefaultUserAgentProvider @Inject constructor(
    private val buildMeta: BuildMeta,
    private val sdkMeta: SdkMetadata,
) : UserAgentProvider {
    private val userAgent: String by lazy { buildUserAgent() }

    override fun provide(): String = userAgent

    /**
     * Create a user agent with the application version.
     * Ex: GetSafeNow/1.0.0 (iPhone 14; iOS 17.0; Build 21A329; Sdk c344b155c)
     */
    private fun buildUserAgent(): String {
        val appName = buildMeta.applicationName
        val appVersion = buildMeta.versionName
        val deviceInfo = getDeviceInfo()
        val platformInfo = getPlatformInfo()
        val buildInfo = getBuildInfo()
        val matrixSdkVersion = sdkMeta.sdkGitSha

        return buildString {
            append(appName)
            append("/")
            append(appVersion)
            append(" (")
            append(deviceInfo)
            append("; ")
            append(platformInfo)
            append("; ")
            append(buildInfo)
            append("; ")
            append("Sdk ")
            append(matrixSdkVersion)
            append(")")
        }
    }

    private fun getDeviceInfo(): String {
        return platformDeviceInfo()
    }

    private fun getPlatformInfo(): String {
        return platformPlatformInfo()
    }

    private fun getBuildInfo(): String {
        return platformBuildInfo()
    }

    // Platform-specific implementations
}

expect fun platformDeviceInfo(): String
expect fun platformPlatformInfo(): String
expect fun platformBuildInfo(): String
