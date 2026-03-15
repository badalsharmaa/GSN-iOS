package com.example.getsafenowclient.utils


import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

object PresenceUtils {

    @OptIn(ExperimentalTime::class)
    fun formatStatus(isOnline: Boolean, lastActiveTimestamp: Long?): String {
        if (isOnline) return "Online"
        
        if (lastActiveTimestamp == null || lastActiveTimestamp <= 0) {
            return "Offline"
        }

        val now = Clock.System.now()
        val lastActive = Instant.fromEpochMilliseconds(lastActiveTimestamp)
        val diff = now - lastActive

        return when {
            diff < 1.minutes -> "Active just now"
            diff < 1.hours -> "Active ${diff.inWholeMinutes}m ago"
            diff < 1.days -> "Active ${diff.inWholeHours}h ago"
            diff < 7.days -> "Active ${diff.inWholeDays}d ago"
            else -> "Offline"
        }
    }
}
