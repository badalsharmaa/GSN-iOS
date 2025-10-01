package io.getsafenow.libraries.gsn_showcase.annotation

/**
 * Marker for @Composable demos to include in a showcase/gallery.
 */
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
@Repeatable
@Suppress("LongParameterList")
annotation class GsnShowCaseComposable(
    val name: String = "",
    val group: String = "",
    val styleName: String = "",
    val widthDp: Int = -1,
    val heightDp: Int = -1,
    val skip: Boolean = false,
    val defaultStyle: Boolean = false,
    val tags: Array<String> = emptyArray(),
    val extraMetadata: Array<String> = emptyArray(),
)