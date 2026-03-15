package com.example.getsafenowclient.utils

/**
 * Unified duration formatter for consistent time display across the app.
 * All duration formatting should use this utility to ensure consistency.
 */
object DurationFormatter {
    
    /**
     * Format duration in milliseconds to m:ss format.
     * 
     * Examples:
     * - 303000ms → "5:03"
     * - 8000ms → "0:08"
     * - 765000ms → "12:45"
     * 
     * @param durationMs Duration in milliseconds
     * @return Formatted string in m:ss format
     */
    fun formatMillis(durationMs: Long): String {
        val totalSeconds = durationMs / 1000
        val m = totalSeconds / 60
        val s = totalSeconds % 60
        return "${m}:${s.toString().padStart(2, '0')}"
    }
    
    /**
     * Format duration in seconds to m:ss format.
     * 
     * Examples:
     * - 303s → "5:03"
     * - 8s → "0:08"
     * - 765s → "12:45"
     * 
     * @param durationSeconds Duration in seconds
     * @return Formatted string in m:ss format
     */
    fun formatSeconds(durationSeconds: Long): String {
        val m = durationSeconds / 60
        val s = durationSeconds % 60
        return "${m}:${s.toString().padStart(2, '0')}"
    }
    
    /**
     * Format duration in seconds (Int) to m:ss format.
     * Convenience overload for Int parameters.
     * 
     * @param durationSeconds Duration in seconds
     * @return Formatted string in m:ss format
     */
    fun formatSeconds(durationSeconds: Int): String {
        return formatSeconds(durationSeconds.toLong())
    }
    
    /**
     * Format duration in seconds to mm:ss format (padded minutes).
     * Used for active call timers where consistent width is desired.
     * 
     * Examples:
     * - 303s → "05:03"
     * - 8s → "00:08"
     * - 765s → "12:45"
     * 
     * @param durationSeconds Duration in seconds
     * @return Formatted string in mm:ss format
     */
    fun formatSecondsPadded(durationSeconds: Long): String {
        val m = durationSeconds / 60
        val s = durationSeconds % 60
        return "${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
    }
}

/**
 * Legacy function names for backward compatibility.
 * These delegate to DurationFormatter methods.
 */

/**
 * Format duration in milliseconds to m:ss format.
 * @deprecated Use DurationFormatter.formatMillis() instead
 */
fun formatDuration(durationMs: Long): String = DurationFormatter.formatMillis(durationMs)

/**
 * Format time in seconds to m:ss format.
 * @deprecated Use DurationFormatter.formatSeconds() instead
 */
fun formatTime(seconds: Int): String = DurationFormatter.formatSeconds(seconds)
