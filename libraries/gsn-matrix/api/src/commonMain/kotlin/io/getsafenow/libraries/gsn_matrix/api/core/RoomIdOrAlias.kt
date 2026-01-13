package io.getsafenow.libraries.gsn_matrix.api.core


import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Immutable
@Serializable
sealed interface RoomIdOrAlias {
    @JvmInline
    @Serializable
    value class Id(val roomId: RoomId) : RoomIdOrAlias

    @JvmInline
    @Serializable
    value class Alias(val roomAlias: RoomAlias) : RoomIdOrAlias

    val identifier: String
        get() = when (this) {
            is Id -> roomId.value
            is Alias -> roomAlias.value
        }
}

fun RoomId.toRoomIdOrAlias() = RoomIdOrAlias.Id(this)
fun RoomAlias.toRoomIdOrAlias() = RoomIdOrAlias.Alias(this)