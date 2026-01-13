package io.getsafenow.libraries.di

import me.tatarka.inject.annotations.Qualifier

/**
 * Qualifies a platform-specific cache directory.
 *
 * - On Android: usually [java.io.File] from `context.cacheDir`
 * - On iOS: often [platform.Foundation.NSTemporaryDirectory] or a derived path
 */
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Qualifier
annotation class CacheDirectoryGsn