package io.getsafenow.libraries.di

/**
 * Legacy owner pattern carried over from Dagger.
 *
 * ⚠️ Not required when using Kotlin Inject in a KMP project.
 * You can directly hold references to your components instead of
 * introducing an "owner" abstraction.
 */
@Deprecated(
    message = "ComponentOwner is not needed with Kotlin Inject. " +
            "Prefer holding your component reference directly.",
    level = DeprecationLevel.WARNING
)
interface ComponentOwnerGsn<T : Any> {
    val component: T
}