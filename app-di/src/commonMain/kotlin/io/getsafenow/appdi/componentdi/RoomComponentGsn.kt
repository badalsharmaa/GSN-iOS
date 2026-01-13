package io.getsafenow.appdi.componentdi

import io.getsafenow.libraries.architecture.ComponentFactoriesBindings
import io.getsafenow.libraries.architecture.ScreenComponent
import io.getsafenow.libraries.architecture.ScreenComponentFactory
import io.getsafenow.libraries.di.RoomScopeGsn
import me.tatarka.inject.annotations.Component
import software.amazon.lastmile.kotlin.inject.anvil.MergeComponent
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn
import kotlin.reflect.KClass

data class RoomArgs(
    val joinedRoomId: String? = null,
    val baseRoomId: String? = null,
)

@SingleIn(RoomScopeGsn::class)
@Component
abstract class RoomComponentGsn(
    @Component val parent: SessionComponentGsn,
    val args: RoomArgs,
) : ComponentFactoriesBindings {

    override fun factories(): Map<KClass<out ScreenComponent>, ScreenComponentFactory<*>> =
        emptyMap<KClass<out ScreenComponent>, ScreenComponentFactory<*>>()

    companion object
}
