package com.example.getsafenowclient.service

import androidx.compose.ui.graphics.ImageBitmap
import co.touchlab.kermit.Logger
import com.russhwolf.settings.Settings
import net.folivo.trixnity.client.media.MediaStore
import org.koin.core.module.Module
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

expect fun getPlatformSettings(): Settings
expect fun ByteArray.asImageBitmap(): ImageBitmap
expect fun getLogger(defaultTag: String): Logger

@OptIn(ExperimentalTime::class)
expect fun createDateFormat(pattern: String): (Instant) -> String
expect fun createRepositoriesModule(): Module
expect fun createMediaStoreModule(): Module

expect fun clearLocalStore()
