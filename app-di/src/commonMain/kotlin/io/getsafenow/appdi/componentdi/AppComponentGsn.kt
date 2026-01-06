package io.getsafenow.appdi.componentdi

import io.getsafenow.libraries.architecture.ComponentFactoriesBindings
import io.getsafenow.libraries.architecture.ScreenComponent
import io.getsafenow.libraries.architecture.ScreenComponentFactory
import io.getsafenow.libraries.di.AppScopeGsn
import io.getsafenow.libraries.gsn_matrix.api.GsnMClient
import io.getsafenow.libraries.kmputils.platformkmp.ContextFactory
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn
import kotlin.reflect.KClass

@SingleIn(AppScopeGsn::class)
@Component
abstract class AppComponentGsn(
    private val contextFactory: ContextFactory,
) : ComponentFactoriesBindings {

    abstract val sessionComponentFactory: SessionComponentFactoryGsn

    @Provides
    fun provideSessionComponentFactory(): SessionComponentFactoryGsn =
        object : SessionComponentFactoryGsn {
            override fun create(client: GsnMClient): SessionComponentGsn =
                createSessionComponent(this@AppComponentGsn, client)
        }

    override fun factories(): Map<KClass<out ScreenComponent>, ScreenComponentFactory<*>> =
        emptyMap()

    companion object
}
