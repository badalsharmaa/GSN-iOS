package io.getsafenow.libraries.di.annotations

import me.tatarka.inject.annotations.Qualifier

/**
 * Qualifies a [kotlinx.coroutines.CoroutineScope] that represents
 * the base scope to use for the whole application.
 */
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Qualifier
annotation class SessionCoroutineScopeGsn
