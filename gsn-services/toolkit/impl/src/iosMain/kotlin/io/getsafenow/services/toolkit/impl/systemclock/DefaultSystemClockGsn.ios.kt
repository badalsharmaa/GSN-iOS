package io.getsafenow.services.toolkit.impl.systemclock


import io.getsafenow.libraries.di.AppScopeGsn
import io.getsafenow.services.toolkit.api.systemclock.SystemClock
import me.tatarka.inject.annotations.Inject
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

@AppScopeGsn
actual class DefaultSystemClockGsn @Inject actual constructor() : SystemClock {
    actual override fun epochMillis(): Long {
        // NSDate gives seconds since 1970 as Double → convert to ms
        return (NSDate().timeIntervalSince1970 * 1000).toLong()
    }
}