package io.getsafenow.libraries.kmputils.bitmap

// Opaque, platform-backed image
expect class KmpImage

// Keep familiar format names
enum class ImageFormat { JPEG, PNG, WEBP }

/** Mirror Android EXIF names to keep call sites familiar */
enum class ExifOrientation {
    ORIENTATION_UNDEFINED,
    ORIENTATION_NORMAL,
    ORIENTATION_ROTATE_90,
    ORIENTATION_ROTATE_180,
    ORIENTATION_ROTATE_270,
    ORIENTATION_FLIP_HORIZONTAL,
    ORIENTATION_FLIP_VERTICAL,
    ORIENTATION_TRANSPOSE,
    ORIENTATION_TRANSVERSE
}

/** Portable write (use path string instead of java.io.File) */
expect fun writeBitmapToPath(
    absolutePath: String,
    bitmap: KmpImage,
    format: ImageFormat,
    quality: Int
)

/** Same semantics as your Bitmap.resizeToMax() */
expect fun KmpImage.resizeToMax(maxWidth: Int, maxHeight: Int): KmpImage

/** Portable inSampleSize calculator */
expect fun calculateInSampleSize(
    outWidth: Int,
    outHeight: Int,
    desiredWidth: Int,
    desiredHeight: Int
): Int

/** Decode + (optionally) downsample, similar to your Options-based flow */
expect fun decodeImageWithSample(
    bytes: ByteArray,
    desiredWidth: Int,
    desiredHeight: Int
): KmpImage

/** Same semantics as your Bitmap.rotateToExifMetadataOrientation() */
expect fun KmpImage.rotateToExifMetadataOrientation(
    orientation: ExifOrientation
): KmpImage
