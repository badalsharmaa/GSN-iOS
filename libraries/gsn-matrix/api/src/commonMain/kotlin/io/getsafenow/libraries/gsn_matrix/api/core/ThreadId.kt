package io.getsafenow.libraries.gsn_matrix.api.core

import io.getsafenow.libraries.kmputils.metadata.isInDebug
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline


@JvmInline
@Serializable
value class ThreadId(val value: String){
    init {
        if (isInDebug && !MPatternsGsn.isThreadId(value)) {
            error(
                "`$value` is not a valid thread id.\n" +
                    "Thread ids are the same as event ids.\n" +
                    "Example thread id: `\$Rqnc-F-dvnEYJTyHq_iKxU2bZ1CI92-kuZq3a5lr5Zg`."
            )
        }
    }

    override fun toString(): String = value
}

fun ThreadId.asEventId(): EventId = EventId(value)
