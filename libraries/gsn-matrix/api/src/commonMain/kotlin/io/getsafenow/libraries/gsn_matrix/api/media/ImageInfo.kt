package io.getsafenow.libraries.gsn_matrix.api.media

data class ImageInfo(
    val height: Long?,
    val width: Long?,
    val mimetype: String?,
    val size: Long?,
    val thumbnailInfo: ThumbnailInfo?,
    val thumbnailSource: MediaSource?,
    val blurhash: String?
)
