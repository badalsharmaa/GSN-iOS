package com.example.getsafenowclient.service

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import co.touchlab.kermit.Logger
import com.example.getsafenowclient.AndroidApp
import com.example.getsafenowclient.database.getDatabaseBuilder
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import kotlinx.coroutines.Dispatchers
import net.folivo.trixnity.client.media.okio.createOkioMediaStoreModule
import net.folivo.trixnity.client.store.repository.room.TrixnityRoomDatabase
import net.folivo.trixnity.client.store.repository.room.createRoomRepositoriesModule
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import org.koin.core.module.Module
import java.text.SimpleDateFormat
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


/**
 * This must be initialized from your Application class once,
 * e.g. in onCreate(): setAppContext(applicationContext)
 */
private lateinit var appContext: Context

fun setAppContext(context: Context) {
    appContext = context.applicationContext
}
actual fun getPlatformSettings(): Settings = SharedPreferencesSettings(
    AndroidApp.INSTANCE.getSharedPreferences("GsnPreferences", Context.MODE_PRIVATE)
)

actual fun ByteArray.asImageBitmap(): ImageBitmap =
    BitmapFactory.decodeByteArray(this, 0, size).asImageBitmap()

actual fun getLogger(defaultTag: String): Logger = Logger.withTag(defaultTag)

@OptIn(ExperimentalTime::class)
actual fun createDateFormat(pattern: String) = object : (Instant) -> String {
    @SuppressLint("SimpleDateFormat")
    private val formatter = SimpleDateFormat(pattern)
    override fun invoke(instant: Instant) = formatter.format(instant.toEpochMilliseconds())
}

private fun getCacheDirectoryPath(): Path =
    AndroidApp.INSTANCE.cacheDir.absolutePath.toPath().resolve("cache")

/*actual fun createRepositoriesModule(): Module {
    val dbBuilder = Room.databaseBuilder(
        context = AndroidApp.INSTANCE,
        klass = TrixnityRoomDatabase::class.java,
        name = "trixnity-database.db"
    )
    return createRoomRepositoriesModule(databaseBuilder = dbBuilder)
}*/

actual fun createRepositoriesModule(): Module {
    // Use your helper to get the database builder
    val dbBuilder: RoomDatabase.Builder<TrixnityRoomDatabase> = getDatabaseBuilder(
        context = AndroidApp.INSTANCE // Replace with your Application context singleton
    )
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)

    // Pass the builder to Trixnity’s repository module creator
    return createRoomRepositoriesModule(databaseBuilder = dbBuilder)
}



actual fun createMediaStoreModule(): Module {
    val basePath = getCacheDirectoryPath().resolve("media")
    return createOkioMediaStoreModule(
        fileSystem = FileSystem.SYSTEM,
        basePath = basePath
    )
}

actual fun clearLocalStore(){
    val dbFile = AndroidApp.INSTANCE.applicationContext.getDatabasePath("trixnity_room.db")
    if (dbFile.exists()) {
        dbFile.delete()
        Logger.withTag("GSN").i("🧹 Cleared old Trixnity database at ${dbFile.absolutePath}")
    }
}

