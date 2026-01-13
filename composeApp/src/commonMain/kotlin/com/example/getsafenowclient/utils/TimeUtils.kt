package com.example.getsafenowclient.utils

import com.example.getsafenowclient.service.createDateFormat
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

// Changed "HH:mm" (24h) to "h:mm a" (12h with AM/PM)
@OptIn(ExperimentalTime::class)
private val timeFormat = createDateFormat("h:mm a")
@OptIn(ExperimentalTime::class)
private val dayFormat = createDateFormat("EEE")
@OptIn(ExperimentalTime::class)
private val fullFormat = createDateFormat("dd/MM/yy")
@OptIn(ExperimentalTime::class)
private val fullDayFormat = createDateFormat("d MMM yyyy")

@OptIn(ExperimentalTime::class)
fun Instant?.toText(): String {
    val tz = TimeZone.currentSystemDefault()
    val date = this?.toLocalDateTime(tz) ?: return ""
    val now = Clock.System.now().toLocalDateTime(tz)
    return when {
        date.date == now.date -> timeFormat(this)
        date.date.weekOfYear() == now.date.weekOfYear() -> dayFormat(this)
        else -> fullFormat(this)
    }
}

@OptIn(ExperimentalTime::class)
fun Instant.fullDayText() = fullDayFormat(this)

@OptIn(ExperimentalTime::class)
fun Instant.timeText() = timeFormat(this)

private fun LocalDate.weekOfYear(): Int {
    val firstDayOfYear = LocalDate(year, 1, 1)
    val daysFromFirstDay = dayOfYear - firstDayOfYear.dayOfYear
    val firstDayOfYearDayOfWeek = firstDayOfYear.dayOfWeek.isoDayNumber
    val adjustment = if (firstDayOfYearDayOfWeek <= 4) {
        firstDayOfYearDayOfWeek - 1
    } else {
        8 - firstDayOfYearDayOfWeek
    }
    return (daysFromFirstDay + adjustment) / 7 + 1
}
