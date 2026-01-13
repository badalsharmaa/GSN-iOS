package io.getsafenow.services.toolkit

import io.getsafenow.services.toolkit.api.intent.NativeIntent
import io.getsafenow.services.toolkit.api.tests.A_FAKE_TIMESTAMP
import io.getsafenow.services.toolkit.api.tests.FakeBuildVersionSdkIntProvider
import io.getsafenow.services.toolkit.api.tests.FakeExternalIntentLauncher
import io.getsafenow.services.toolkit.api.tests.FakeSystemClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for [FakeExternalIntentLauncher].
 *
 * These tests verify that the launcher correctly delegates to the
 * provided lambda when `launch` is invoked with a [NativeIntent].
 */
class ExternalIntentLauncherTest {
    @Test
    fun `launch calls provided lambda`() {
        var called = false
        val launcher = FakeExternalIntentLauncher { _ -> called = true }

        // Use the no-arg ctor to avoid Android framework calls in host JVM tests
        launcher.launch(NativeIntent())

        assertTrue(called, "Expected launch lambda to be invoked")
    }
}


/**
 * Unit tests for [FakeBuildVersionSdkIntProvider].
 *
 * These tests ensure that the fake SDK version provider
 * behaves correctly when simulating Android API level checks.
 */
class BuildVersionSdkIntProviderTest {

    @Test
    fun `whenAtLeast returns result when satisfied`() {
        val provider = FakeBuildVersionSdkIntProvider(sdkInt = 33)
        val result = provider.whenAtLeast(30) { "OK" }
        assertEquals("OK", result)
    }

    @Test
    fun `whenAtLeast returns null when not satisfied`() {
        val provider = FakeBuildVersionSdkIntProvider(sdkInt = 29)
        val result = provider.whenAtLeast(30) { "NOPE" }
        assertNull(result)
    }

    @Test
    fun `isAtLeast works for equals and greater`() {
        val provider = FakeBuildVersionSdkIntProvider(sdkInt = 30)
        assertTrue(provider.isAtLeast(30))
        assertTrue(provider.isAtLeast(29))
        assertFalse(provider.isAtLeast(31))
    }
}


/**
 * Unit tests for [FakeSystemClock].
 *
 * These tests validate that the fake clock correctly
 * returns and updates its epoch time as configured.
 */
class SystemClockTest {

    @Test
    fun `epochMillis returns configured time`() {
        val clock = FakeSystemClock(epochMillisResult = A_FAKE_TIMESTAMP)
        assertEquals(A_FAKE_TIMESTAMP, clock.epochMillis())
    }

    @Test
    fun `epochMillis increases when time is advanced`() {
        val clock = FakeSystemClock(epochMillisResult = 1000L)
        clock.epochMillisResult = 1500L
        assertEquals(1500L, clock.epochMillis())
    }

    @Test
    fun `epochMillis overrides time when set`() {
        val clock = FakeSystemClock(epochMillisResult = 0L)
        clock.epochMillisResult = 42L
        assertEquals(42L, clock.epochMillis())
    }
}



