package io.getsafenow.libraries.di

import me.tatarka.inject.annotations.Scope
import kotlin.reflect.KClass

/**
 * Legacy ElementX-style custom scope that indicated
 * a single instance within a given Dagger component.
 *
 * ⚠️ Not required when using Kotlin Inject in KMP.
 * Prefer using @Singleton or explicit @Scope annotations.
 */
@Deprecated(
    message = "SingleIn is not needed with Kotlin Inject. " +
            "Use @Singleton or define a specific @Scope instead.",
    level = DeprecationLevel.WARNING
)
@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class SingleInGsn(val clazz: KClass<*>)