package com.example.getsafenowclient.service

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import co.touchlab.kermit.Logger
import com.example.getsafenowclient.database.getDatabaseBuilder
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import net.folivo.trixnity.client.media.okio.createOkioMediaStoreModule
import net.folivo.trixnity.client.store.repository.room.createRoomRepositoriesModule
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import org.jetbrains.skia.Image
import org.koin.core.module.Module
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUserDomainMask
import platform.Foundation.dateWithTimeIntervalSince1970
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


// Helper function to get the base directory path for iOS (Documents directory)
@OptIn(ExperimentalForeignApi::class)
fun getIosDatabasePath(databaseName: String): String {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null
    )?.path ?: throw IllegalStateException("Document directory not found")

    return "$documentDirectory/$databaseName"
}
actual fun getPlatformSettings(): Settings = NSUserDefaultsSettings(
    NSUserDefaults("GsnSettings")
)

actual fun ByteArray.asImageBitmap(): ImageBitmap =
    Image.makeFromEncoded(this).toComposeImageBitmap()

actual fun getLogger(defaultTag: String): Logger = Logger.withTag(defaultTag)

@OptIn(ExperimentalTime::class)
actual fun createDateFormat(pattern: String): (instant: Instant) -> String =
    object : (Instant) -> String {
        private val formatter = NSDateFormatter().apply {
            setDateFormat(pattern)
        }

        override fun invoke(instant: Instant) = formatter.stringFromDate(
            NSDate.dateWithTimeIntervalSince1970(instant.epochSeconds.toDouble())
        )
    }

private fun getCacheDirectoryPath(): Path {
    val cacheDir = NSSearchPathForDirectoriesInDomains(
        NSCachesDirectory,
        NSUserDomainMask,
        true
    ).first() as String
    return "$cacheDir/cache".toPath()
}

actual fun createRepositoriesModule(): Module {
    val dbBuilder = getDatabaseBuilder()
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)

    return createRoomRepositoriesModule(databaseBuilder = dbBuilder)
}
actual fun createMediaStoreModule(): Module {
    val mediaDirPath = getCacheDirectoryPath().resolve("media")
    return createOkioMediaStoreModule(
        fileSystem = FileSystem.SYSTEM,
        basePath = mediaDirPath
    )
}

@OptIn(ExperimentalForeignApi::class)
actual fun clearLocalStore() {
    val docs = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null
    )
    val path = requireNotNull(docs?.path) + "/trixnity_room.db"
    val fileMgr = NSFileManager.defaultManager
    if (fileMgr.fileExistsAtPath(path)) {
        fileMgr.removeItemAtPath(path, null)
        Logger.withTag("GSN").i("🧹 Cleared old Trixnity database at $path")
    }
}

