package com.example.getsafenowclient.common.readreceipts

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.core.model.EventId
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.events.m.ReceiptType

/**
 * Sends read receipts + read markers.
 */
class ReadReceiptSender(
    private val client: MatrixClient,
    private val scope: CoroutineScope
) {

    fun markRead(roomId: RoomId, eventId: EventId) {
        // Launch independently every time, non-cancellable
        scope.launch(Dispatchers.IO + NonCancellable) {
            try {
                client.api.room.setReceipt(
                    roomId = roomId,
                    eventId = eventId,
                    receiptType = ReceiptType.Read
                )
            } catch (_: Exception) { /* ignore */ }

            try {
                client.api.room.setReadMarkers(
                    roomId = roomId,
                    fullyRead = eventId,
                    read = eventId,
                    privateRead = null
                )
            } catch (_: Exception) { /* ignore */ }
        }
    }
}
