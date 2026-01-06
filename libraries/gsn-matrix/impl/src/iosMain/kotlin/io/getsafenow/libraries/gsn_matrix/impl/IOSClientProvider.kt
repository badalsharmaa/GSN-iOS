package io.getsafenow.libraries.gsn_matrix.impl

actual class PlatformClientBuilder {
    private lateinit var nativeClientBuilder: NativeClientBuilder

    init {
        nativeClientBuilder = nativeClientBuilderProvider() // Call the function directly
    }

    val clientBuilder: NativeClientBuilder
        get() = nativeClientBuilder
}

// Interface for iOS injection
interface NativeClientBuilder

lateinit var nativeClientBuilderProvider: () -> NativeClientBuilder