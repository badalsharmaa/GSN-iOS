package io.getsafenow.libraries.gsn_matrix.api.core

import io.getsafenow.libraries.kmputils.metadata.isInDebug
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
value class EventId(val value: String){
    init {
        if (isInDebug && !MPatternsGsn.isEventId(value)) {
            error("`$value` is not a valid event id.\nExample event id: `\$Rqnc-F-dvnEYJTyHq_iKxU2bZ1CI92-kuZq3a5lr5Zg`.")
        }
    }
    override fun toString(): String = value
}
fun EventId.toThreadId(): ThreadId = ThreadId(value)
