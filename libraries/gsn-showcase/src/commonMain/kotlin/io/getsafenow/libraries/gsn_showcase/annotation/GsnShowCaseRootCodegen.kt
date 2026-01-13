package io.getsafenow.libraries.gsn_showcase.annotation

/**
 * Internal metadata annotation for a **showcase root**.
 *
 * This is intended for codegen/tooling to record aggregate counts discovered in your
 * module (e.g., number of composables, colors, typography entries). It is **not**
 * meant for direct use in app code.
 *
 * Retention is **RUNTIME** to allow optional reflective discovery by debug utilities.
 * If you don't plan to read it via reflection (especially on Kotlin/Native), you may
 * switch to **BINARY** retention safely.
 *
 * @property numComposablesWithoutPreviewParameter Count of `@GsnShowCaseComposable` entries
 *           whose demos take **no** preview parameter.
 * @property numComposablesWithPreviewParameter Count of `@GsnShowCaseComposable` entries
 *           whose demos **do** take a preview parameter.
 * @property numColors Count of `@GsnShowCaseColor` entries.
 * @property numTypography Count of `@GsnShowCaseTypography` entries.
 */
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class GsnShowCaseRootCodegen(
    val numComposablesWithoutPreviewParameter: Int,
    val numComposablesWithPreviewParameter: Int,
    val numColors: Int,
    val numTypography: Int
)

