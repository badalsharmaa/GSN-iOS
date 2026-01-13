package io.getsafenow.libraries.architecture.overlay.operation

import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import io.getsafenow.libraries.architecture.overlay.Overlay

/**
 * Shows an overlay by activating the given element/config.
 */
data class Show<T : Any>(private val element: T) : OverlayOperation<T> {
    override fun apply(overlay: SlotNavigation<T>) {
        overlay.activate(element)
    }
}

/** Call on SlotNavigation directly. */
fun <T : Any> SlotNavigation<T>.show(element: T) = accept(Show(element))

/** Convenience: call on your Overlay class too. */
fun <T : Any> Overlay<T>.show(element: T) = navigation.show(element)
