package io.getsafenow.services.toolkit.impl.sdk

import android.os.Build
import io.getsafenow.libraries.di.AppScopeGsn
import io.getsafenow.services.toolkit.api.sdk.BuildVersionSdkIntProvider
import me.tatarka.inject.annotations.Inject

@AppScopeGsn
actual class `DefaultBuildVersionSdkIntProviderGsn` @Inject actual constructor():
    BuildVersionSdkIntProvider {
    actual override fun get(): Int = Build.VERSION.SDK_INT
}