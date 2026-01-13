package io.getsafenow.services.toolkit.impl.systemclock

import io.getsafenow.services.toolkit.api.systemclock.SystemClock


/**
 * Expect default implementation of [SystemClock].
 * Actuals are provided per platform.
 */
expect class DefaultSystemClockGsn() : SystemClock {
    override fun epochMillis(): Long
}