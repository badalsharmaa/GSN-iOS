package io.getsafenow.libraries.di.annotations

import me.tatarka.inject.annotations.Qualifier

/**
 * Qualifies a [kotlinx.coroutines.CoroutineScope] that represents
 * the base scope to use for an active Matrix session.
 */
@MustBeDocumented
@Qualifier
annotation class AppCoroutineScopeGsn
