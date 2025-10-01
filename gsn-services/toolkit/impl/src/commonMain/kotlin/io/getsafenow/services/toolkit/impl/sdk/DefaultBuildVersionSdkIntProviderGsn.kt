package io.getsafenow.services.toolkit.impl.sdk

import io.getsafenow.services.toolkit.api.sdk.BuildVersionSdkIntProvider


/**
 * Expect default implementation of [BuildVersionSdkIntProvider].
 * Actuals are provided per platform.
 */
expect class DefaultBuildVersionSdkIntProviderGsn() : BuildVersionSdkIntProvider {
    override fun get(): Int
}