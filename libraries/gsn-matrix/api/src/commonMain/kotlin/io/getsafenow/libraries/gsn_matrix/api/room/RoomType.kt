package io.getsafenow.libraries.gsn_matrix.api.room

sealed interface RoomType {
    data object Space : RoomType
    data object Room : RoomType
    data class Other(val type: String) : RoomType
}
