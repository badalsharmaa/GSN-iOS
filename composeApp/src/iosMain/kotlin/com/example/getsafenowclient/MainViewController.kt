package com.example.getsafenowclient

import androidx.compose.ui.window.ComposeUIViewController
import com.example.getsafenowclient.call.CallKitController
import com.example.getsafenowclient.call.PushKitManager
import com.example.getsafenowclient.call.SharedCallManager
import io.getsafenow.libraries.kmputils.platformkmp.ContextFactory
import platform.UIKit.UIViewController

// We need to keep references to prevent GC
private var pushKitManager: PushKitManager? = null
private var callKitController: CallKitController? = null

fun MainViewController(): UIViewController {
    // Initialize Call Feature Singletons for iOS
    if (callKitController == null) {
        val controller = CallKitController(
             onAnswer = {
                  // Dispatch to callbacks registered by CallBackgroundManager (if active)
                  // We need a mechanism for CallBackgroundManager to register its callbacks to this singleton
                 SharedCallManager.onAnswerCallback?.invoke()
             },
             onHangup = {
                  SharedCallManager.onHangupCallback?.invoke()
             }
        )
        callKitController = controller
        SharedCallManager.controller = controller

        // PushKit needs the controller to report calls
        pushKitManager = PushKitManager(controller)
    }

    return ComposeUIViewController {
        App(
            contextFactory = ContextFactory()
            // intentExtras not needed for iOS standard launch usually,
            // handled by CallKit interaction
        )
    }
}