package io.getsafenow.libraries.gsn_matrix.api.media

data class FileInfo(
    val mimetype: String?,
    val size: Long?,
    val thumbnailInfo: ThumbnailInfo?,
    val thumbnailSource: MediaSource?
)
