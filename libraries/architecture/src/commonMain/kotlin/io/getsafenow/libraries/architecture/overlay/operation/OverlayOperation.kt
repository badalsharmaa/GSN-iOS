package io.getsafenow.libraries.architecture.overlay.operation

import com.arkivanov.decompose.router.slot.SlotNavigation

/**
 * Decompose compat: an "operation" that mutates the overlay (ChildSlot)
 * by acting on Decompose's SlotNavigation<T>.
 */
interface OverlayOperation<T : Any> {
    fun apply(overlay: SlotNavigation<T>)
}

/** Run an OverlayOperation on this overlay controller. */
fun <T : Any> SlotNavigation<T>.accept(op: OverlayOperation<T>) {
    op.apply(this)
}
