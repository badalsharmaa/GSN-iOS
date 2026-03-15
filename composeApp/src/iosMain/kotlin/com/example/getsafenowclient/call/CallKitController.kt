package com.example.getsafenowclient.call

import kotlinx.cinterop.ExperimentalForeignApi
import platform.CallKit.CXAnswerCallAction
import platform.CallKit.CXCallUpdate
import platform.CallKit.CXEndCallAction
import platform.CallKit.CXHandle
import platform.CallKit.CXHandleTypeGeneric
import platform.CallKit.CXProvider
import platform.CallKit.CXProviderConfiguration
import platform.CallKit.CXProviderDelegateProtocol
import platform.Foundation.NSUUID
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
class CallKitController(
    private val onAnswer: () -> Unit,
    private val onHangup: () -> Unit
) : NSObject(), CXProviderDelegateProtocol {

    private val provider: CXProvider

    init {
        val config = CXProviderConfiguration("GetSafeNow").apply {
            supportsVideo = true
            maximumCallsPerCallGroup = 1u
            supportedHandleTypes = setOf(CXHandleTypeGeneric)
        }
        provider = CXProvider(configuration = config)
        provider.setDelegate(this, queue = null)
    }

    fun reportIncomingCall(uuid: NSUUID, handle: String, hasVideo: Boolean) {
        val update = CXCallUpdate().apply {
            remoteHandle = CXHandle(CXHandleTypeGeneric, handle)
            this.hasVideo = hasVideo
            localizedCallerName = handle 
        }

        provider.reportNewIncomingCallWithUUID(uuid, update) { error ->
            if (error != null) {
                // Handle error
                println("CallKit: Failed to report incoming call: $error")
            } else {
                println("CallKit: Incoming call reported successfully")
            }
        }
    }
    
    fun endCall(uuid: NSUUID) {
        // Logic to notify system that call ended would go here if we were initiating the end
        // But predominantly we use this to respond to system actions.
        // To programmatically end, we'd need a CXCallController.
    }

    // MARK: CXProviderDelegateProtocol

    override fun providerDidReset(provider: CXProvider) {
        // Stop audio
        onHangup()
    }

    override fun provider(provider: CXProvider, performAnswerCallAction: CXAnswerCallAction) {
        // Signal answering
        onAnswer()
        performAnswerCallAction.fulfill()
    }

    override fun provider(provider: CXProvider, performEndCallAction: CXEndCallAction) {
        // Signal hangup
        onHangup()
        performEndCallAction.fulfill()
    }
}
