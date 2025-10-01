package io.getsafenow.libraries.architecture.overlay

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.KSerializer

/**
 * Overlay nav container (Appyx-like) built on Decompose.
 * Holds a single on-top child that can be activated (show) or dismissed (hide).
 *
 * @param serializer Optional serializer for saving/restoring the overlay config (recommended if you want state restore).
 */
class Overlay<NavTarget : Any>(
    componentContext: ComponentContext,
    key: String = Overlay::class.qualifiedName ?: "Overlay",
    serializer: KSerializer<NavTarget>? = null,
) : ComponentContext by componentContext {

    /** Controller to activate/dismiss the overlay. */
    val navigation: SlotNavigation<NavTarget> = SlotNavigation()

    /**
     * Current overlay slot. `slot.value.child` is non-null when an overlay is shown.
     * Child instance is the config itself (NavTarget) for maximum generality.
     */
    val slot: Value<ChildSlot<NavTarget, NavTarget>> =
        childSlot(
            source = navigation,
            serializer = serializer,        // enables state save/restore when provided
            handleBackButton = true,        // back will dismiss when active
            key = key,
        ) { config, _ -> config }           // child == config; wrap if you need a component
}
