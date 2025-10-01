package io.getsafenow.libraries.di

import me.tatarka.inject.annotations.Qualifier

/**
 * Qualifies a platform-specific "application context".
 *
 * - On Android: an [android.content.Context] tied to the Application.
 * - On iOS: you might provide a [platform.Foundation.NSBundle] or similar root object.
 */
@MustBeDocumented
@Qualifier
annotation class ApplicationContextGsn
