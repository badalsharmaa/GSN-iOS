package io.getsafenow.libraries.gsn_matrix.impl

import io.getsafenow.libraries.di.AppScopeGsn
import me.tatarka.inject.annotations.Inject
import software.amazon.lastmile.kotlin.inject.anvil.ContributesBinding


interface ClientBuilderProvider {
    fun provide(): PlatformClientBuilder
}

expect class PlatformClientBuilder()

@ContributesBinding(AppScopeGsn::class)
class RustClientBuilderProvider @Inject constructor() : ClientBuilderProvider {
    override fun provide(): PlatformClientBuilder {
        return PlatformClientBuilder()
    }
}
