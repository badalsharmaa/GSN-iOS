package io.getsafenow.libraries.architecture

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import io.getsafenow.libraries.architecture.ScreenComponent

/**
 * Marker interface for a feature’s public entrypoint.
 * Expose this from DI so callers can create the feature's root component
 * without leaking internal types.
 */
interface FeatureEntryPoint

/**
 * Simple feature that exposes a single ScreenComponent (auto-rendered).
 */
interface SimpleFeatureEntryPoint : FeatureEntryPoint {
    /**
     * Create the feature’s root ScreenComponent, scoped to a child ComponentContext.
     *
     * @param parent the parent ComponentContext to scope this feature under.
     * @param key a stable key for lifecycle/state (defaults to the feature’s FQCN).
     */
    fun createComponent(
        parent: ComponentContext,
        key: String = this::class.qualifiedName ?: "Feature"
    ): ScreenComponent
}

/* --- Optional helper: common childContext pattern --- */
fun ComponentContext.featureChildContext(key: String): ComponentContext =
    childContext(key)
