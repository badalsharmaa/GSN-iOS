package io.getsafenow.libraries.gsn_showcase.annotation

/**
 * Marker for color entries (e.g., Color definitions) to include in a showcase/gallery.
 *
 * @param name  Optional display name; defaults to the property name.
 * @param group Optional grouping key; defaults to the enclosing type name or "Default Group".
 */
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
annotation class GsnShowCaseColor(
    val name: String = "",
    val group: String = "",
)