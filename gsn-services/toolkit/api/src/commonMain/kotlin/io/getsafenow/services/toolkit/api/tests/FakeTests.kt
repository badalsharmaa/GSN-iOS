package io.getsafenow.services.toolkit.api.tests

import io.getsafenow.services.toolkit.api.intent.ExternalIntentLauncher
import io.getsafenow.services.toolkit.api.intent.NativeIntent
import io.getsafenow.services.toolkit.api.sdk.BuildVersionSdkIntProvider
import io.getsafenow.services.toolkit.api.systemclock.SystemClock

//test helpers-
expect fun nativeIntentFromUrl(url: String): NativeIntent
/**
 * Fake [ExternalIntentLauncher] for use in tests.
 *
 * Lets you provide a lambda to observe/verify launched intents.
 */
class FakeExternalIntentLauncher(
    var launchLambda: (NativeIntent) -> Unit = { error("launch() not configured") },
) : ExternalIntentLauncher {
    override fun launch(intent: NativeIntent) {
        launchLambda(intent)
    }
}

/**
 * Fake [BuildVersionSdkIntProvider] for tests.
 *
 * Allows you to control the reported SDK version.
 */
class FakeBuildVersionSdkIntProvider(
    private val sdkInt: Int
) : BuildVersionSdkIntProvider {
    override fun get(): Int = sdkInt
}

/**
 * Fake [SystemClock] for tests.
 *
 * Allows manual control over the reported time.
 */
const val A_FAKE_TIMESTAMP: Long = 123L

class FakeSystemClock(
    var epochMillisResult: Long = A_FAKE_TIMESTAMP
) : SystemClock {
    override fun epochMillis(): Long = epochMillisResult
}