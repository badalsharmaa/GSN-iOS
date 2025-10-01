package io.getsafenow.services.toolkit.impl.sdk

import io.getsafenow.libraries.di.AppScopeGsn
import io.getsafenow.services.toolkit.api.sdk.BuildVersionSdkIntProvider
import me.tatarka.inject.annotations.Inject
import platform.UIKit.UIDevice

@AppScopeGsn
actual class DefaultBuildVersionSdkIntProviderGsn @Inject actual constructor() :
    BuildVersionSdkIntProvider {
    actual override fun get(): Int {
        // Example: "17.5" -> 17
        val versionString = UIDevice.currentDevice.systemVersion
        val major = versionString.substringBefore(".").toIntOrNull() ?: 0
        return major
    }
}