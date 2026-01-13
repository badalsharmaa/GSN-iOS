package io.getsafenow.services.toolkit.impl.intent


import android.content.Context
import android.content.Intent
import io.getsafenow.libraries.di.AppScopeGsn
import io.getsafenow.libraries.di.ApplicationContextGsn
import io.getsafenow.services.toolkit.api.intent.ExternalIntentLauncher
import io.getsafenow.services.toolkit.api.intent.NativeIntent
import me.tatarka.inject.annotations.Inject


// Actual PlatformContext holds Android Context
actual class PlatformContext(
    val ctx: Context
)

// Actual launcher implementation
@AppScopeGsn
actual class DefaultExternalIntentLauncherGsn @Inject actual constructor(
    @param:ApplicationContextGsn  private val context: PlatformContext,
) : ExternalIntentLauncher {
    actual override fun launch(intent: NativeIntent) {
        context.ctx.startActivity(
            intent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}