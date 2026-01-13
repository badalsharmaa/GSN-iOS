package com.example.getsafenowclient.database

import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import net.folivo.trixnity.client.store.repository.room.TrixnityRoomDatabase
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask


@OptIn(ExperimentalForeignApi::class)
fun getDatabaseBuilder(): RoomDatabase.Builder<TrixnityRoomDatabase> {
    val docs = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null
    )
    val dbPath = requireNotNull(docs?.path) + "/trixnity_room.db"
    return Room.databaseBuilder<TrixnityRoomDatabase>(
        name = dbPath
    )
}