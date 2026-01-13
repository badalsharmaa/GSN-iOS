package io.getsafenow.libraries.gsn_matrix.api.timeline.item.event

import androidx.compose.runtime.Immutable
import io.getsafenow.libraries.gsn_matrix.api.core.EventId
import io.getsafenow.libraries.gsn_matrix.api.core.TransactionId
import kotlin.jvm.JvmInline

@Immutable
sealed interface EventOrTransactionId {
    @JvmInline
    value class Event(val id: EventId) : EventOrTransactionId

    @JvmInline
    value class Transaction(val id: TransactionId) : EventOrTransactionId

    val eventId: EventId?
        get() = (this as? Event)?.id

    companion object {
        fun from(eventId: EventId?, transactionId: TransactionId?): EventOrTransactionId {
            return when {
                eventId != null -> Event(eventId)
                transactionId != null -> Transaction(transactionId)
                else -> throw IllegalArgumentException("EventId and TransactionId are both null")
            }
        }
    }
}

fun EventId.toEventOrTransactionId() = EventOrTransactionId.Event(this)
fun TransactionId.toEventOrTransactionId() = EventOrTransactionId.Transaction(this)
