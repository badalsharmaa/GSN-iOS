package com.example.getsafenowclient.call

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSData
import platform.Foundation.NSDictionary
import platform.Foundation.NSError
import platform.Foundation.NSUUID
import platform.PushKit.*
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
class PushKitManager(
    private val callKitController: CallKitController
) : NSObject(), PKPushRegistryDelegateProtocol {

    private val registry: PKPushRegistry

    init {
        registry = PKPushRegistry(queue = null)
        registry.setDelegate(this)
        registry.desiredPushTypes = setOf(PKPushTypeVoIP)
    }

    // MARK: PKPushRegistryDelegate

    override fun pushRegistry(registry: PKPushRegistry, didUpdatePushCredentials: PKPushCredentials, forType: String?) {
        val tokenData = didUpdatePushCredentials.token
        // TODO: Convert tokenData to String and send to Matrix Homeserver as "pusher"
        // This is usually done by the main app logic via Trixnity
        println("PushKit: Got VoIP Token")
    }

    override fun pushRegistry(registry: PKPushRegistry, didReceiveIncomingPushWithPayload: PKPushPayload, forType: String?, withCompletionHandler: () -> Unit) {
        val payload = didReceiveIncomingPushWithPayload.dictionaryPayload
        
        // Extract Matrix/Sygnal data
        // Sygnal usually puts data in 'data' key or root depending on config
        // Assuming standard structure:
        
        val callId = payload["call_id"] as? String
        val roomId = payload["room_id"] as? String
        val callerName = payload["sender_display_name"] as? String ?: payload["caller_name"] as? String ?: "Unknown"

        if (callId != null || roomId != null) {
            val uuid = NSUUID() // In production, derive this deterministically from callId to avoid duplicates
            callKitController.reportIncomingCall(uuid, callerName, hasVideo = true)
        }
        
        withCompletionHandler()
    }
}
