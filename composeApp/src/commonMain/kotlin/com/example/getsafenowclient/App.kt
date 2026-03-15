package com.example.getsafenowclient

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.getsafenowclient.auth.AuthScreen
import com.example.getsafenowclient.auth.authPresenter
import com.example.getsafenowclient.call.CallEvent
import com.example.getsafenowclient.call.CallScreen
import com.example.getsafenowclient.call.CallState
import com.example.getsafenowclient.component.LoadingScreen
import com.example.getsafenowclient.component.OnboardingScreens
import com.example.getsafenowclient.di.createComposeAppComponent
import com.example.getsafenowclient.home.HomeRoot
import com.example.getsafenowclient.service.SessionManager
import io.getsafenow.libraries.gsn_theme.customtheme.GsnTheme
import io.getsafenow.libraries.kmputils.platformkmp.ContextFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

internal val LocalAppScope = androidx.compose.runtime.compositionLocalOf<CoroutineScope> {
    error("LocalAppScope is not provided")
}
internal val LocalPhoneMode: androidx.compose.runtime.ProvidableCompositionLocal<MutableState<Boolean>> =
    androidx.compose.runtime.compositionLocalOf { mutableStateOf(false) }


@OptIn(ExperimentalTime::class)
@Composable
@Preview
fun App(contextFactory: ContextFactory, intentExtras: Map<String, Any?>? = null) {
    // DI: Resolve dependencies via Kotlin Inject
    val component = remember { createComposeAppComponent(contextFactory) }
    val sessionManager: SessionManager = remember { component.sessionManager }
    val roomComponentStore = remember { component.roomComponentStore }
    val contextFactoryImpl = remember { component.contextFactory }

    val appScope = rememberCoroutineScope()
    var isPhone by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf<OnboardingScreens?>(null) }

    // ✅ FIX: Track initialization to prevent loading screen on app resume
    var hasInitialized by remember { mutableStateOf(false) }

    // Try to restore the session automatically at startup
    LaunchedEffect(Unit) {
        // ✅ Only run initialization ONCE (not on every recomposition)
        if (!hasInitialized) {
            hasInitialized = true

            val minDisplayTime = 2.5.seconds
            val startTime = Clock.System.now()

            try {
                // Directly check session restoration
                val restored = sessionManager.tryRestoreSession()
                val nextScreen =
                    if (restored) OnboardingScreens.MainApp else OnboardingScreens.Login

                // ✅ Only enforce minimum display time if we actually restored a session
                // This prevents showing "Loading..." for 2.5s on fresh installs
                if (restored) {
                    val elapsedTime = Clock.System.now() - startTime
                    if (elapsedTime < minDisplayTime) {
                        delay(minDisplayTime - elapsedTime)
                    }
                }

                currentScreen = nextScreen

                // ✅ Request notification permission after login/restore
                if (currentScreen == OnboardingScreens.MainApp) {
                    appScope.launch {
                        delay(500)  // Small delay to ensure UI is ready
                        try {
                            val notificationPermission = component.notificationPermission
                            if (!notificationPermission.hasPermission()) {
                                val granted = notificationPermission.requestPermission()
                                if (granted) {
                                    co.touchlab.kermit.Logger.d { "Notification permission granted" }
                                } else {
                                    co.touchlab.kermit.Logger.w { "Notification permission denied - notifications won't work" }
                                }
                            }
                        } catch (e: Exception) {
                            co.touchlab.kermit.Logger.e(e) { "Failed to request notification permission" }
                        }
                    }
                }

                // ✅ Handle Incoming Call Intent with State Checking (after screen is set)
                if (restored && intentExtras != null) {
                    val isIncoming = intentExtras["EXTRA_IS_INCOMING"] as? Boolean == true
                    val callId = intentExtras["EXTRA_CALL_ID"] as? String
                    val callerName = intentExtras["EXTRA_CALLER_NAME"] as? String

                    if (callId != null) {
                        appScope.launch {
                            // Small delay to ensure CallModel is initialized
                            delay(100)

                            val currentState = component.callModel.state.value.callState

                            when {
                                // Fresh launch - show call UI
                                currentState is CallState.Idle -> {
                                    component.callModel.resumeIncomingCall(
                                        callId = callId,
                                        callerName = callerName,
                                        isVideo = true,
                                        isIncoming = isIncoming
                                    )
                                }

                                // Already ringing/connecting/in call with SAME callId - just restore UI
                                (currentState is CallState.IncomingRinging && currentState.callId == callId) ||
                                        (currentState is CallState.OutgoingRinging && currentState.callId == callId) ||
                                        (currentState is CallState.Connecting && currentState.callId == callId) ||
                                        (currentState is CallState.InCall && currentState.callId == callId) ||
                                        (currentState is CallState.Reconnecting && currentState.callId == callId) -> {
                                    // Just un-minimize, don't create duplicate state
                                    component.callModel.dispatch(CallEvent.Restore)
                                }

                                // Call already ended - ignore intent
                                currentState is CallState.Ended -> {
                                    co.touchlab.kermit.Logger.i { "Call $callId already ended, ignoring intent" }
                                }

                                // Different call active - log warning
                                else -> {
                                    co.touchlab.kermit.Logger.w { "Unexpected state $currentState for call $callId from intent" }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // ✅ Handle session restore failures gracefully
                co.touchlab.kermit.Logger.e(e) { "Session restore failed" }
                currentScreen = OnboardingScreens.Login
            }
        }
    }

        // Clean up sync when app is closed
        DisposableEffect(Unit) {
            onDispose { appScope.launch { sessionManager.stopSync() } }
        }

        CompositionLocalProvider(
            LocalAppScope provides appScope,
            LocalPhoneMode provides mutableStateOf(false)
        ) {
            GsnTheme {
                Box(modifier = Modifier.fillMaxSize()) {

                    // FIX: Resolve the call model only when we are in the MainApp screen.
                    // This prevents DI from trying to access MatrixClient before login/restore.
                    val callModel = remember(currentScreen) {
                        if (currentScreen == OnboardingScreens.MainApp) {
                            component.callModel
                        } else null
                    }

                    // 1. Main Application Content with Transitions
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            fadeIn(tween(300)) +
                                    scaleIn(
                                        initialScale = 0.95f,
                                        animationSpec = tween(300)
                                    ) togetherWith
                                    fadeOut(tween(300))
                        },
                        label = "AppScreenTransition"
                    ) { target ->
                        when (target) {
                            null -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(GsnTheme.colors.bgCanvasDefault),
                                    contentAlignment = Alignment.Center
                                ) {
                                    LoadingScreen(loadingMessage = "Loading...")
                                }
                            }

                            OnboardingScreens.Login -> {
                                val (state, eventSink) = authPresenter(
                                    sessionManager = sessionManager,
                                    onLoginSuccess = { currentScreen = OnboardingScreens.MainApp },
                                )
                                AuthScreen(
                                    state = state,
                                    eventSink = eventSink,
                                )
                            }

                            OnboardingScreens.MainApp -> {
                                val client = remember { sessionManager.getClient() }

                                // callModel is guaranteed non-null here by the remember block above
                                callModel?.let { model ->
                                    HomeRoot(
                                        sessionManager = sessionManager,
                                        roomComponentStore = roomComponentStore,
                                        client = client,
                                        contextFactory = contextFactoryImpl,
                                        callModel = model,
                                        isPhoneMode = isPhone,
                                        onRequireLogin = { currentScreen = OnboardingScreens.Login }
                                    )
                                }
                            }

                            else -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(GsnTheme.colors.bgCanvasDefault),
                                    contentAlignment = Alignment.Center
                                ) {
                                    LoadingScreen(loadingMessage = "Loading...")
                                }
                            }
                        }
                    }

                    // 2. Global Call UI Layer (Floats over EVERYTHING)
                    // Only show if we are in MainApp and have a valid call model
                    if (currentScreen == OnboardingScreens.MainApp && callModel != null) {
                        val client = remember { sessionManager.getClient() }
                        CallScreen(
                            component = callModel,
                            client = client,
                            modifier = Modifier
                        )
                    }

                    // 3. Global Snackbar Host (In-App Notifications)
                    com.example.getsafenowclient.component.GsnSnackbarHost(
                        hostState = com.example.getsafenowclient.notification.GlobalSnackbarState.hostState,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )

                    // 4. Global Sync Indicator (Shows when Matrix SDK syncs)
                    // This appears at the very top, above all content
                    if (currentScreen == OnboardingScreens.MainApp) {
                        val isSyncing by component.globalSyncManager.isSyncing.collectAsState()
                        com.example.getsafenowclient.component.GlobalSyncIndicator(
                            isSyncing = isSyncing,
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    }
                }
            }
        }
    }
