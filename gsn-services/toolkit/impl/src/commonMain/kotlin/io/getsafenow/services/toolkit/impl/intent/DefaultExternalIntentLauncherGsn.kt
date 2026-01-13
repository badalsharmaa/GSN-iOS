package io.getsafenow.services.toolkit.impl.intent

import io.getsafenow.libraries.di.ApplicationContextGsn
import io.getsafenow.services.toolkit.api.intent.ExternalIntentLauncher
import io.getsafenow.services.toolkit.api.intent.NativeIntent
import me.tatarka.inject.annotations.Inject


/**
 * Expect class to wrap platform context.
 * - Android: wraps android.content.Context
 * - iOS: empty container (can later wrap UIApplication/NSBundle if needed)
 */
expect class PlatformContext

/**
 * Default implementation of [ExternalIntentLauncher].
 * Actuals are provided per platform.
 */
expect class DefaultExternalIntentLauncherGsn(
    context: PlatformContext
) : ExternalIntentLauncher {
    override fun launch(intent: NativeIntent)
}