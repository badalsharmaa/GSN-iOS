package io.getsafenow.libraries.gsn_matrix.api.roomdirectory

import kotlinx.coroutines.CoroutineScope

interface RoomDirectoryService {
    fun createRoomDirectoryList(scope: CoroutineScope): RoomDirectoryList
}
