package io.getsafenow.appdi.componentdi

import io.getsafenow.libraries.architecture.ComponentFactoriesBindings
import io.getsafenow.libraries.architecture.ScreenComponent
import io.getsafenow.libraries.architecture.ScreenComponentFactory
import io.getsafenow.libraries.di.SessionScopeGsn
import io.getsafenow.libraries.gsn_matrix.api.GsnMClient
import io.getsafenow.libraries.gsn_matrix.api.user.CurrentSessionIdHolder
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn
import kotlin.reflect.KClass

@SingleIn(SessionScopeGsn::class)
@Component
abstract class SessionComponentGsn(
    @Component val parent: AppComponentGsn,
    val client: GsnMClient,
) : ComponentFactoriesBindings {

    @Provides
    fun provideClient(): GsnMClient = client

    abstract val currentSessionIdHolder: CurrentSessionIdHolder

    abstract val roomComponentFactory: RoomComponentFactoryGsn

    @Provides
    fun provideRoomComponentFactory(): RoomComponentFactoryGsn =
        object : RoomComponentFactoryGsn {
            override fun create(args: RoomArgs): RoomComponentGsn =
                createRoomComponents(parent= this@SessionComponentGsn,args)
        }

    override fun factories(): Map<KClass<out ScreenComponent>, ScreenComponentFactory<*>> =
        emptyMap<KClass<out ScreenComponent>, ScreenComponentFactory<*>>()

    companion object
}
