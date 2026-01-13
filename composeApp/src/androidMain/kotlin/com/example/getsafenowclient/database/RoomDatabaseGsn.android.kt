package com.example.getsafenowclient.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import net.folivo.trixnity.client.store.repository.room.TrixnityRoomDatabase

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<TrixnityRoomDatabase> {
    val dbPath = context.applicationContext.getDatabasePath("trixnity_room.db")
    return Room.databaseBuilder<TrixnityRoomDatabase>(
        context = context,
        name = dbPath.absolutePath
    )
}