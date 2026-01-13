package io.getsafenow.libraries.architecture

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.StackAnimation
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import io.getsafenow.libraries.architecture.overlay.HideOverlayBackPressHandler
import io.getsafenow.libraries.architecture.overlay.Overlay
import kotlinx.serialization.KSerializer

/** A child that can render itself, Appyx-style. */
interface ScreenComponent {
    @Composable fun Render()
}

/**
 * "Flow container" that holds a back stack + an overlay (Decompose),
 * with Appyx-style auto rendering of children.
 */
@Stable
abstract class BaseFlowNode<NavTarget : Any>(
    componentContext: ComponentContext,
    /** Initial back-stack configuration. */
    initial: NavTarget,
    /** Optional serializer for NavTarget to enable state save/restore. */
    private val serializer: KSerializer<NavTarget>? = null,
    /** Builds a screen component for a given NavTarget (back stack). */
    private val childFactory: (NavTarget, ComponentContext) -> ScreenComponent,
    /**
     * Builds an overlay component for a given NavTarget, or null if that NavTarget has no overlay UI.
     * If not provided, overlays are not rendered.
     */
    val overlayFactory: (NavTarget, ComponentContext) -> ScreenComponent? = { _, _ -> null },
    /**
     * Permanent children always present alongside the flow.
     * Key = stable id; Value = builder using a child ComponentContext.
     */
    permanentChildren: Map<String, (ComponentContext) -> Any> = emptyMap(),
) : ComponentContext by componentContext {

    /** Back stack navigation controller. */
    val navigation: StackNavigation<NavTarget> = StackNavigation()

    /** Back stack state (active + back entries). */
    val stack: Value<ChildStack<NavTarget, ScreenComponent>> = childStack(
        source = navigation,
        initialConfiguration = initial,
        serializer = serializer,
        handleBackButton = true,
        childFactory = { config, ctx -> childFactory(config, ctx) }
    )

    /** Overlay holder (single on-top child that can be shown/dismissed). */
    val overlay: Overlay<NavTarget> = Overlay(componentContext = this, serializer = serializer)

    /** Permanent children built once for this flow node. */
    val permanent: Map<String, Any> = buildPermanent(permanentChildren)

    /** Back handler that consumes Back to hide overlay if one is shown. */
    @Suppress("unused")
    private val hideOverlayBackHandler = HideOverlayBackPressHandler(this, overlay)

    init {
        lifecycle.doOnCreate { logLifecycle("${this@BaseFlowNode::class.simpleName} created") }
        lifecycle.doOnDestroy { logLifecycle("${this@BaseFlowNode::class.simpleName} destroyed") }
    }

    private fun buildPermanent(
        builders: Map<String, (ComponentContext) -> Any>
    ): Map<String, Any> = builders.mapValues { (key, builder) -> builder(childContext(key)) }

    // ---- Convenience navigation APIs ----

    /** Push a new screen onto the back stack. */
    @OptIn(DelicateDecomposeApi::class)
    fun push(target: NavTarget) = navigation.push(target)

    /** Replace the current screen with a new one. */
    fun replaceCurrent(target: NavTarget) = navigation.replaceCurrent(target)

    /** Pop the back stack if possible. */
    fun pop() = navigation.pop()

    /** Whether the back stack can pop at least one entry. */
    fun canPop(): Boolean = stack.value.backStack.isNotEmpty()

    /** Logging hook (override with your logger if desired). */
    protected open fun logLifecycle(msg: String) { /* no-op by default */ }
}

/** Back stack renderer with a springy slide — auto-renders ScreenComponent.Render(). */
@Composable
fun <NavTarget : Any> BaseFlowNode<NavTarget>.BackstackView(
    modifier: Modifier = Modifier,
    animation: StackAnimation<NavTarget, ScreenComponent> = stackAnimation(
        slide(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
    ),
) {
    Children(
        modifier = modifier,
        stack = stack,
        animation = animation
    ) { child ->
        child.instance.Render()
    }
}

/**
 * Overlay renderer.
 * Decompose has no built-in slot animation; we use AnimatedVisibility (fade) by default.
 * The overlay is created from [overlayFactory] on demand and auto-rendered via ScreenComponent.Render().
 */
@Composable
fun <NavTarget : Any> BaseFlowNode<NavTarget>.OverlayView(
    modifier: Modifier = Modifier,
    fade: Boolean = true,
) {
    val slot by overlay.slot.subscribeAsState()
    val config = slot.child?.instance

    if (config == null) {
        // nothing to show
        return
    }

    // Build (or remember) a ScreenComponent for this config.
    // Use a stable child context derived from the config to scope lifecycle/state.
    val key = "overlay:${config.hashCode()}"
    val component = remember(config) {
        overlayFactory(config, childContext(key))
            ?: return@remember null
    }

    if (component == null) return

    if (fade) {
        AnimatedVisibility(
            modifier = modifier,
            visible = true,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            component.Render()
        }
    } else {
        Box(modifier) { component.Render() }
    }
}

/** Convenience to layer back stack + overlay, then optional extra content. */
@Composable
fun <NavTarget : Any> BaseFlowNode<NavTarget>.BackstackWithOverlayBox(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {},
) {
    Box(modifier = modifier) {
        BackstackView()
        OverlayView()
        content()
    }
}
