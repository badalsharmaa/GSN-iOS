package io.getsafenow.libraries.gsn_showcase.annotation

import kotlin.reflect.KClass

/**
 * Marks a **screenshot test runner** class for your showcase.
 *
 * Use this on a class that coordinates taking screenshots of the components
 * registered in your showcase (root declared via [GsnShowCaseRoot]). This is a
 * lightweight, KMP-safe marker intended for codegen or tooling that will
 * discover the annotated class and drive screenshot capture.
 *
 * ### Typical usage
 * ```kotlin
 * @GsnShowcaseScreenshot(rootShowCaseClass = MyShowcaseRoot::class)
 * abstract class MyScreenshotTest /* : YourScreenshotModule */ {
 *     // Implement your screenshot capture/handling here.
 * }
 * ```
 *
 * ### Notes
 * - Keep this annotation in **commonMain** so both Android & iOS builds can see it.
 * - If your screenshot pipeline is Android-specific (uses Bitmaps, instrumented tests),
 *   place the **implementation** class in `androidTest` and keep only the annotation
 *   and root reference in shared code.
 *
 * @param rootShowCaseClass The KClass of your showcase root (the class annotated
 * with [GsnShowCaseRoot]) that should be used as the source of components for screenshots.
 */
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class GsnShowcaseScreenshot(
    val rootShowCaseClass: KClass<*>
)