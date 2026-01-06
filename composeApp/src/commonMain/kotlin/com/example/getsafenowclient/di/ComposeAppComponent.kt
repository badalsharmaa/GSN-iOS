package com.example.getsafenowclient.di

import co.touchlab.kermit.Logger
import com.example.getsafenowclient.call.CallScreenModel
import com.example.getsafenowclient.call.CallSignalingHandler
import com.example.getsafenowclient.call.webrtc.WebRtcManager
import com.example.getsafenowclient.common.hardware.CallRingtoneController
import com.example.getsafenowclient.common.hardware.SpeakerController
import com.example.getsafenowclient.room.di.RoomComponentStore
import com.example.getsafenowclient.service.SessionManager
import com.example.getsafenowclient.service.getLogger
import com.example.getsafenowclient.service.getPlatformSettings
import com.russhwolf.settings.Settings
import io.getsafenow.libraries.kmputils.platformkmp.ContextFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.KmpComponentCreate
import me.tatarka.inject.annotations.Provides
import me.tatarka.inject.annotations.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class AppScope

@AppScope
@Component
abstract class ComposeAppComponent(
    @get:Provides val contextFactory: ContextFactory
) {
    // 1. Ensure SessionManager is a singleton in this component
    abstract val sessionManager: SessionManager
    
    @AppScope
    @Provides
    fun provideRoomComponentStore(): RoomComponentStore = RoomComponentStore()

    abstract val roomComponentStore: RoomComponentStore

    // 📞 Expose the global call model
    abstract val callModel: CallScreenModel

    @Provides fun provideSettings(): Settings = getPlatformSettings()
    @Provides fun provideLogger(): Logger = getLogger("GSN")

    @AppScope
    @Provides
    fun provideAppScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    @AppScope
    @Provides
    fun provideWebRtcManager(scope: CoroutineScope, sessionManager: SessionManager): WebRtcManager {
        return WebRtcManager(
            scope = scope,
            turnProvider = { sessionManager.getTurnServerCached() }
        ).apply {
            this.speakerController = SpeakerController(contextFactory)
            this.ringtoneController = CallRingtoneController(contextFactory)
        }
    }

    @AppScope
    @Provides
    fun provideCallSignalingHandler(
        sessionManager: SessionManager,
        webrtc: WebRtcManager,
        scope: CoroutineScope,
        callModelProvider: () -> CallScreenModel
    ): CallSignalingHandler {
        return CallSignalingHandler(
            clientProvider = { sessionManager.getClient() },
            webrtc = webrtc,
            scope = scope,
            // 📞 FIX: Use the actual deviceId as the partyId
            myPartyId = sessionManager.deviceId ?: "unknown_device",
            dispatch = { event, roomId ->
                val model = callModelProvider()
                if (event is com.example.getsafenowclient.call.CallEvent.IncomingInvite) {
                    model.handleIncomingInvite(event, roomId)
                } else {
                    model.dispatch(event)
                }
            }
        )
    }

    @AppScope
    @Provides
    fun provideCallScreenModel(
        webrtc: WebRtcManager,
        signaling: CallSignalingHandler,
        scope: CoroutineScope,
        sessionManager: SessionManager
    ): CallScreenModel {
        return CallScreenModel(
            webrtc = webrtc,
            signaling = signaling,
            scope = scope,
            clientProvider = { sessionManager.getClient() }
        )
    }
}

@KmpComponentCreate
expect fun createComposeAppComponent(contextFactory: ContextFactory): ComposeAppComponent
