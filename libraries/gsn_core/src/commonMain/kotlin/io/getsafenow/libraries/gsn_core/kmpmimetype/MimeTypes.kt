package io.getsafenow.libraries.gsn_core.kmpmimetype

import io.getsafenow.libraries.gsn_core.helpers.defaultFalse

// The Android SDK does not provide constant for mime type, add some of them here
object MimeTypes {
    const val Any: String = "*/*"
    const val OctetStream = "application/octet-stream"
    const val Apk = "application/vnd.android.package-archive"
    const val Pdf = "application/pdf"

    const val Images = "image/*"

    const val Png = "image/png"
    const val BadJpg = "image/jpg"
    const val Jpeg = "image/jpeg"
    const val Gif = "image/gif"
    const val WebP = "image/webp"
    const val Svg = "image/svg+xml"

    const val Videos = "video/*"
    const val Mp4 = "video/mp4"

    const val Audio = "audio/*"

    const val Ogg = "audio/ogg"
    const val Mp3 = "audio/mp3"
    const val AudioMp4 = "audio/mp4"
    const val M4a = "audio/m4a"

    const val PlainText = "text/plain"

    fun String?.normalizeMimeType() = if (this == BadJpg) Jpeg else this

    fun String?.isMimeTypeImage() = this?.startsWith("image/").defaultFalse()
    fun String?.isMimeTypeAnimatedImage() = this == Gif || this == WebP
    fun String?.isMimeTypeVideo() = this?.startsWith("video/").defaultFalse()
    fun String?.isMimeTypeAudio() = this?.startsWith("audio/").defaultFalse()
    fun String?.isMimeTypeApplication() = this?.startsWith("application/").defaultFalse()
    fun String?.isMimeTypeFile() = this?.startsWith("file/").defaultFalse()
    fun String?.isMimeTypeText() = this?.startsWith("text/").defaultFalse()
    fun String?.isMimeTypeAny() = this?.startsWith("*/").defaultFalse()

    fun fromFileExtension(fileExtension: String): String {
        return when (fileExtension.lowercase()) {
            "apk" -> Apk
            "pdf" -> Pdf
            "png" -> Png
            "jpg", "jpeg" -> Jpeg
            "gif" -> Gif
            "webp" -> WebP
            "svg" -> Svg
            "mp4" -> Mp4
            "m4a" -> AudioMp4
            "ogg" -> Ogg
            "mp3" -> Mp3
            else -> OctetStream
        }
    }

    fun toFileExtension(mimeType: String?): String {
        return when (mimeType?.lowercase()) {
            Apk -> "apk"
            Pdf -> "pdf"
            Png -> "png"
            Jpeg, BadJpg -> "jpg"
            Gif -> "gif"
            WebP -> "webp"
            Svg -> "svg"
            Mp4 -> "mp4"
            AudioMp4, M4a -> "m4a"
            Ogg -> "ogg"
            Mp3 -> "mp3"
            else -> "bin"
        }
    }

    fun hasSubtype(mimeType: String): Boolean {
        val components = mimeType.split("/")
        if (components.size != 2) return false
        val subType = components.last()
        return subType.isNotBlank() && subType != "*"
    }
}
