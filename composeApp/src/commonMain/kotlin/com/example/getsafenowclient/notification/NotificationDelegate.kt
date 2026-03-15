package com.example.getsafenowclient.notification

import co.touchlab.kermit.Logger
import com.example.getsafenowclient.di.AppScope
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.tatarka.inject.annotations.Inject
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.client.notification
import net.folivo.trixnity.core.model.events.ClientEvent.RoomEvent
import net.folivo.trixnity.core.model.events.m.call.CallEventContent
import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent

private val log = KotlinLogging.logger("NotificationDelegate")

@AppScope
class NotificationDelegate @Inject constructor(
    private val platformNotificationManager: PlatformNotificationManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun startListening(client: MatrixClient) {
        Logger.i { "Starting notification listener..." }
        
        // Use client.notification (NotificationService) extension
        client.notification.getNotifications()
            .onEach { notification ->
                processNotification(notification.event)
            }
            .launchIn(scope)
    }

    private fun processNotification(event: net.folivo.trixnity.core.model.events.ClientEvent<*>) {
        if (event is RoomEvent.MessageEvent<*>) {
           when (val content = event.content) {
                // ✅ REMOVED: Call handling - CallScreenModel already handles this via CallSignalingHandler
                // Keeping this would create DUPLICATE notifications (one from CallScreenModel, one here)
                
                is RoomMessageEventContent.TextBased.Text -> {
                    Logger.i { "Received Message via NotificationService: ${event.id}" }
                    platformNotificationManager.showMessageNotification(
                        roomId = event.roomId.full,
                        senderName = event.sender.full,
                        messageBody = content.body,
                        eventId = event.id.full
                    )
                }
                else -> {
                    // Other event types ignored
                }
           }
        }
    }
}
