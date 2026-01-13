package com.example.getsafenowclient.home.setting.avatar

sealed interface AvatarChangeEvent {
    data class AvatarSelected(val bytes: ByteArray) : AvatarChangeEvent {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as AvatarSelected

            return bytes.contentEquals(other.bytes)
        }

        override fun hashCode(): Int {
            return bytes.contentHashCode()
        }
    }
    data object ClearAvatar : AvatarChangeEvent
    data object DismissError : AvatarChangeEvent
}
