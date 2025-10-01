package io.getsafenow.libraries.gsn_showcase.annotation


/**
 * Marker interface for the **root** of your showcase/catalog within a module.
 *
 * Annotate exactly one implementation per module with [GsnShowCaseRoot] so tools
 * (or your own runtime registry) know where to start aggregating all annotated
 * showcase entries (composables, colors, typography) across a multi-module KMP project.
 *
 * ### Usage
 * ```
 * @GsnShowCaseRoot
 * class MyShowcaseRoot : GsnShowCaseRootModule
 * ```
 *
 * This interface is intentionally empty; it exists purely as a **type marker**.
 * Keep it in **commonMain** so both Android and iOS can reference it.
 */
interface GsnShowCaseRootModule