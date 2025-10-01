// commonMain/kotlin/io/getsafenow/libraries/gsn_showcase/annotation/ShowkaseTypography.kt
package io.getsafenow.libraries.gsn_showcase.annotation


/**
 * Marker for typography entries (e.g., TextStyle definitions) to include in a showcase/gallery.
 *
 * @param name  Optional display name; defaults to the property name.
 * @param group Optional grouping key; defaults to the enclosing type name or "Default Group".
 */
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
annotation class GsnShowCaseTypography(
    val name: String = "",
    val group: String = "",
)