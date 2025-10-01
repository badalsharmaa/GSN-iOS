package io.getsafenow.features.api

import com.arkivanov.decompose.ComponentContext
import io.getsafenow.libraries.architecture.FeatureEntryPoint
import io.getsafenow.libraries.architecture.ScreenComponent

/**
 * Feature entrypoint for the Login flow (KMP, Decompose).
 * Exposes a builder so callers can pass Params + Callback without leaking internal classes.
 */
interface LoginEntryPoint : FeatureEntryPoint {

    data class Params(
        val accountProvider: String?, // e.g. tenant or homeserver domain
        val loginHint: String?,       // e.g. prefilled username/email
    )

    /** Replaces Appyx Plugin-based callbacks. Keep simple, testable, KMP-friendly. */
    interface Callback {
        fun onReportProblem()
    }

    /**
     * Create a builder scoped under the given parent context.
     * Use a stable key to isolate lifecycle/state if multiple logins can exist.
     */
    fun componentBuilder(
        parent: ComponentContext,
        key: String = this::class.qualifiedName ?: "Login"
    ): ComponentBuilder

    interface ComponentBuilder {
        fun params(params: Params): ComponentBuilder
        fun callback(callback: Callback): ComponentBuilder
        fun build(): ScreenComponent
    }
}