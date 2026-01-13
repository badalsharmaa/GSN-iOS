package com.example.getsafenowclient.room

import co.touchlab.kermit.Logger
import io.getsafenow.libraries.kmputils.platformkmp.PlatformFile
import io.ktor.http.ContentType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.client.room
import net.folivo.trixnity.client.room.message.audio
import net.folivo.trixnity.client.room.message.text
import net.folivo.trixnity.client.room.message.video
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.events.m.room.ThumbnailInfo
import okio.Path.Companion.toPath
import okio.SYSTEM
import okio.buffer
import okio.use

/**
 * Handles the logic for sending content (Text, Video, Audio) to a Matrix Room.
 * This extracts the low-level I/O and API calls from the ScreenModel.
 */
class RoomContentSender(
    private val client: MatrixClient,
    private val logger: Logger
) {

    suspend fun sendMessage(roomId: RoomId, text: String, onSuccess: suspend () -> Unit = {}) {
        try {
            client.room.sendMessage(roomId) { text(text) }
            delay(50)
            onSuccess()
        } catch (e: Exception) {
            logger.e(e) { "Failed to send message" }
        }
    }

    suspend fun sendVideo(
        roomId: RoomId,
        file: PlatformFile,
        duration: Long,
        thumbnail: PlatformFile?,
        onSuccess: suspend () -> Unit = {}
    ) {
        if (!file.exists()) return

        try {
            val path = file.path.toPath()

            // --- Stream file as chunks ---
            val CHUNK_SIZE = 1024 * 1024 // 1 MB per chunk for video

            val fileSize = okio.FileSystem.SYSTEM.metadata(path).size ?: 0L

            val uploadFlow = flow {
                okio.FileSystem.SYSTEM.source(path).buffer().use { source ->
                    val buffer = ByteArray(CHUNK_SIZE)
                    while (true) {
                        val count = source.read(buffer)
                        if (count == -1) break
                        emit(buffer.copyOf(count))
                    }
                }
            }

            // Thumbnail handling
            var thumbFlow: Flow<ByteArray>? = null
            var thumbInfo: ThumbnailInfo? = null

            if (thumbnail != null && thumbnail.exists()) {
                val thumbPath = thumbnail.path.toPath()
                val thumbSize = okio.FileSystem.SYSTEM.metadata(thumbPath).size ?: 0L

                thumbFlow = flow {
                    okio.FileSystem.SYSTEM.source(thumbPath).buffer().use { source ->
                        val buffer = ByteArray(1024 * 64)
                        while (true) {
                            val count = source.read(buffer)
                            if (count == -1) break
                            emit(buffer.copyOf(count))
                        }
                    }
                }

                thumbInfo = ThumbnailInfo(
                    size = thumbSize,
                    mimeType = "image/jpeg",
                    height = null,
                    width = null
                )
            }

            client.room.sendMessage(roomId) {
                video(
                    body = "Video Message",
                    video = uploadFlow,
                    type = ContentType("video", "mp4"),
                    size = fileSize,
                    duration = duration,
                    thumbnail = thumbFlow,
                    thumbnailInfo = thumbInfo,
                    height = null,
                    width = null
                )
            }

            delay(50)
            onSuccess()

        } catch (e: Exception) {
            logger.e(e) { "Failed to send video message" }
        }
    }

    suspend fun sendAudio(
        roomId: RoomId,
        file: PlatformFile,
        duration: Long,
        onSuccess: suspend () -> Unit = {}
    ) {
        if (!file.exists()) return

        try {
            val path = file.path.toPath()
            val CHUNK_SIZE = 32 * 1024
            val fileSize = okio.FileSystem.SYSTEM.metadata(path).size ?: 0L

            val uploadFlow = flow {
                okio.FileSystem.SYSTEM.source(path).buffer().use { source ->
                    val buffer = ByteArray(CHUNK_SIZE)
                    while (true) {
                        val count = source.read(buffer)
                        if (count == -1) break
                        emit(buffer.copyOf(count))
                    }
                }
            }

            client.room.sendMessage(roomId) {
                audio(
                    body = "Voice Message",
                    audio = uploadFlow,
                    type = ContentType("audio", "m4a"),
                    size = fileSize,
                    duration = duration
                )
            }

            delay(50)
            onSuccess()

        } catch (e: Exception) {
            logger.e(e) { "Failed to send voice message" }
        }
    }
}
