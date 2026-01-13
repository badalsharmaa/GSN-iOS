package io.getsafenow.libraries.gsn_showcase.annotation

/**
 * Marks the **root module** for your Compose showcase/catalog.
 *
 * This annotation is placed on a class that implements your showcase root marker
 * interface (e.g., [GsnShowCaseRootModule]). It lets tooling or your own runtime
 * registry know **where to start aggregating** all annotated components, colors,
 * and typography across modules in a multi-module KMP project.
 *
 * ### Rules
 * - Annotate **exactly one** class per module with `@GsnShowCaseRoot`.
 * - The annotated class should implement your root marker interface
 *   (e.g., [GsnShowCaseRootModule]).
 * - Keep this in a **shared (commonMain)** source set so Android and iOS builds
 *   can both discover it.
 *
 * ### Example (KMP)
 * ```kotlin
 * // commonMain
 * package io.getsafenow.libraries.gsn_showcase
 *
 * import io.getsafenow.libraries.gsn_showcase.annotation.GsnShowCaseRoot
 *
 * interface GsnShowCaseRootModule
 *
 * @GsnShowCaseRoot
 * class GsnShowcaseRoot : GsnShowCaseRootModule
 * ```
 *
 * In your debug “GsnShowCase” screen you can locate this root (via DI or a simple
 * registry) and use it to build the gallery tree on both Android and iOS.
 */
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class GsnShowCaseRoot