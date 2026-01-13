package io.getsafenow.services.toolkit.api.intent


expect class NativeIntent()
/**
 * Used to launch external intents from anywhere in the app.
 */
interface ExternalIntentLauncher {
    fun launch(intent: NativeIntent) // abstract type, actual platform decides what `intent` is
}
