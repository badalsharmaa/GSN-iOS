package io.getsafenow.services.toolkit.impl.intent


import io.getsafenow.libraries.di.AppScopeGsn
import io.getsafenow.libraries.di.ApplicationContextGsn
import io.getsafenow.services.toolkit.api.intent.ExternalIntentLauncher
import io.getsafenow.services.toolkit.api.intent.NativeIntent
import me.tatarka.inject.annotations.Inject
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

// iOS doesn't need to hold a Context (empty container for DI)
actual class PlatformContext(
    val app: UIApplication = UIApplication.sharedApplication
)

// Actual launcher implementation
@AppScopeGsn
actual class DefaultExternalIntentLauncherGsn @Inject actual constructor(
    @param:ApplicationContextGsn  private val context: PlatformContext,
) : ExternalIntentLauncher {
    actual override fun launch(intent: NativeIntent) {
        val nsUrl = NSURL(string = intent.url)
        nsUrl.let { context.app.openURL(it) }
    }
}