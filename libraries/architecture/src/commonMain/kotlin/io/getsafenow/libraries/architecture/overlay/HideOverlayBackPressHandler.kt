package io.getsafenow.libraries.architecture.overlay


import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.value.subscribe
import com.arkivanov.essenty.backhandler.BackCallback
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.doOnDestroy

/**
 * Back-press strategy: if an overlay is showing, consume back and dismiss it.
 *
 * Decompose notes:
 * - Decompose/Essenty uses BackHandler + a BackCallback that's enabled/disabled.
 */
class HideOverlayBackPressHandler<NavTarget : Any>(
    componentContext: ComponentContext,
    private val overlay: Overlay<NavTarget>,
) {
    private val backHandler: BackHandler = componentContext.backHandler
    private val callback = BackCallback {
        overlay.navigation.dismiss()
    }

    init {
        // Register callback and keep its enabled state in sync with whether a child is shown
        backHandler.register(callback)
        callback.isEnabled = overlay.slot.value.child != null

        // Subscribe to slot changes; enable only when overlay has a child
        overlay.slot.subscribe(componentContext.lifecycle) { slot ->
            callback.isEnabled = slot.child != null
        }

        // Clean up automatically
        componentContext.lifecycle.doOnDestroy {
            backHandler.unregister(callback)
        }
    }
}
