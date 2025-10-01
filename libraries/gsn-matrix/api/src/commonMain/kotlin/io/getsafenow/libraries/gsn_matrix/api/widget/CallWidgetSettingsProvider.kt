package io.getsafenow.libraries.gsn_matrix.api.widget

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
interface CallWidgetSettingsProvider {
    suspend fun provide(
        baseUrl: String,
        widgetId: String = Uuid.random().toString(),
        encrypted: Boolean,
        direct: Boolean,
    ): MatrixWidgetSettings
}
