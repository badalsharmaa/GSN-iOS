package io.getsafenow.libraries.architecture.decomposer

import androidx.compose.runtime.Composable
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.Lifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow

/**
 * Starts a Molecule presenter bound to this component's lifecycle and returns a StateFlow of State.
 * Uses RecompositionMode.Immediate for KMP safety (no frame clock required).
 */
fun <State> ComponentContext.launchMolecule(
    mode: RecompositionMode = RecompositionMode.Immediate,
    block: @Composable () -> State,
): StateFlow<State> {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    lifecycle.subscribe(object : Lifecycle.Callbacks {
        override fun onDestroy() {
            scope.cancel()
        }
    })

    // NOTE: Parameter name is `block` in Molecule, not `body`.
    return scope.launchMolecule<State>(mode) { block() }
}
