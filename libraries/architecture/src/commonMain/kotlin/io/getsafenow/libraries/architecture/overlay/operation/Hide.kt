package io.getsafenow.libraries.architecture.overlay.operation

import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.dismiss
import io.getsafenow.libraries.architecture.overlay.Overlay


/**
 * Hides (dismisses) the currently active overlay/modal.
 */
class Hide<T : Any> : OverlayOperation<T> {
    override fun apply(overlay: SlotNavigation<T>) {
        overlay.dismiss()
    }
    override fun equals(other: Any?): Boolean = this::class == other?.let { it::class }
    override fun hashCode(): Int = this::class.hashCode()
}

/** Call on SlotNavigation directly. */
fun <T : Any> SlotNavigation<T>.hide() = accept(Hide())

/** Convenience: call on your Overlay class too. */
fun <T : Any> Overlay<T>.hide() = navigation.hide()