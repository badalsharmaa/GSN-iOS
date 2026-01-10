package com.example.getsafenowclient

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.getsafenowclient.auth.AuthScreen
import com.example.getsafenowclient.auth.authPresenter
import com.example.getsafenowclient.call.CallScreen
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
fun App(contextFactory: ContextFactory) {
    // DI: Resolve dependencies via Kotlin Inject
    val component = remember { createComposeAppComponent(contextFactory) }
    val sessionManager: SessionManager = remember { component.sessionManager }
    val roomComponentStore = remember { component.roomComponentStore }
    val contextFactoryImpl = remember { component.contextFactory }

    val appScope = rememberCoroutineScope()
    var isPhone by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf<OnboardingScreens?>(null) }

    // Try to restore the session automatically at startup
    LaunchedEffect(Unit) {
        val minDisplayTime = 2.5.seconds
        val startTime = Clock.System.now()

        // Directly check session restoration
        val restored = sessionManager.tryRestoreSession()
        val nextScreen = if (restored) OnboardingScreens.MainApp else OnboardingScreens.Login

        val elapsedTime = Clock.System.now() - startTime
        if (elapsedTime < minDisplayTime) {
            delay(minDisplayTime - elapsedTime)
        }

        currentScreen = nextScreen
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

                // 1. Main Application Content
                when (currentScreen) {
                    null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(GsnTheme.colors.bgCanvasDefault),
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingScreen(loadingMessage = "Updating data...")
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
            }
        }
    }
}
