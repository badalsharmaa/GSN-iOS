package io.getsafenow.libraries.preferences.api

import io.getsafenow.libraries.gsn_matrix.api.media.MediaPreviewValue
import io.getsafenow.libraries.gsn_matrix.api.tracing.LogLevel
import io.getsafenow.libraries.gsn_matrix.api.tracing.TraceLogPack
import kotlinx.coroutines.flow.Flow


interface AppPreferencesStore {
    suspend fun setDeveloperModeEnabled(enabled: Boolean)
    fun isDeveloperModeEnabledFlow(): Flow<Boolean>

    suspend fun setCustomElementCallBaseUrl(string: String?)
    fun getCustomElementCallBaseUrlFlow(): Flow<String?>

    suspend fun setTheme(theme: String)
    fun getThemeFlow(): Flow<String?>

    @Deprecated("Use MediaPreviewService instead. Kept only for migration.")
    suspend fun setHideInviteAvatars(hide: Boolean?)
    @Deprecated("Use MediaPreviewService instead. Kept only for migration.")
    fun getHideInviteAvatarsFlow(): Flow<Boolean?>
    @Deprecated("Use MediaPreviewService instead. Kept only for migration.")
    suspend fun setTimelineMediaPreviewValue(mediaPreviewValue: MediaPreviewValue?)
    @Deprecated("Use MediaPreviewService instead. Kept only for migration.")
    fun getTimelineMediaPreviewValueFlow(): Flow<MediaPreviewValue?>

    suspend fun setTracingLogLevel(logLevel: LogLevel)
    fun getTracingLogLevelFlow(): Flow<LogLevel>

    suspend fun setTracingLogPacks(targets: Set<TraceLogPack>)
    fun getTracingLogPacksFlow(): Flow<Set<TraceLogPack>>

    suspend fun reset()
}
