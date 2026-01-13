package io.getsafenow.libraries.architecture

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import io.getsafenow.libraries.architecture.ScreenComponent
import kotlin.reflect.KClass

/**
 * Factory contract for building a ScreenComponent scoped to a parent ComponentContext.
 */
interface ScreenComponentFactory<N : ScreenComponent> {
    fun create(parent: ComponentContext): N
}

/**
 * A registry (replaces the old Dagger 'bindings' map).
 * Put all your feature/component factories in here via DI.
 */
interface ComponentFactoriesBindings {
    fun factories(): Map<KClass<out ScreenComponent>, ScreenComponentFactory<*>>
}

/**
 * Create a ScreenComponent of type [N] using the registry.
 * - Uses a child ComponentContext with a stable [key].
 * - Throws if factory is missing (clear error like the old code).
 */
inline fun <reified N : ScreenComponent> ComponentFactoriesBindings.createComponent(
    parent: ComponentContext,
    key: String = N::class.qualifiedName ?: "Component"
): N {
    val k = N::class
    val factory = factories()[k]
        ?: error("Cannot find ScreenComponentFactory for ${k.qualifiedName}. Did you register it in ComponentFactoriesBindings?")
    @Suppress("UNCHECKED_CAST")
    return (factory as ScreenComponentFactory<N>).create(parent.childContext(key))
}
