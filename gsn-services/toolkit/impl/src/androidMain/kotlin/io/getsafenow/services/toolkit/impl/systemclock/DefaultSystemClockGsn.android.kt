package io.getsafenow.services.toolkit.impl.systemclock

import io.getsafenow.libraries.di.AppScopeGsn
import io.getsafenow.services.toolkit.api.systemclock.SystemClock
import me.tatarka.inject.annotations.Inject

@AppScopeGsn
actual class DefaultSystemClockGsn @Inject actual constructor() : SystemClock {
    /**
     * Provides a UTC epoch in milliseconds.
     *
     * This can be overridden by user device settings,
     * so it's not guaranteed to be monotonic.
     */
    actual override fun epochMillis(): Long = System.currentTimeMillis()
}