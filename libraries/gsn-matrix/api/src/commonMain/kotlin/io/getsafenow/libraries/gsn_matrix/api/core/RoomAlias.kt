package io.getsafenow.libraries.gsn_matrix.api.core

import io.getsafenow.libraries.kmputils.metadata.isInDebug
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline


@JvmInline
@Serializable
value class RoomAlias(val value: String) {
    init {
        if (isInDebug && !MPatternsGsn.isRoomAlias(value)) {
            error("`$value` is not a valid room alias.\n Example room alias: `#room_alias:domain`.")
        }
    }

    override fun toString(): String = value
}
