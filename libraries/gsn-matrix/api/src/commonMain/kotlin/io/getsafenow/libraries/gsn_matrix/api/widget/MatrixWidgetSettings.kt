package io.getsafenow.libraries.gsn_matrix.api.widget

import kotlinx.serialization.Serializable


@Serializable
class MatrixWidgetSettings(
    val id: String,
    val initAfterContentLoad: Boolean,
    val rawUrl: String,
)  {
    companion object
}
