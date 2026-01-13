package io.getsafenow.libraries.kmputils.bitmap

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.core.graphics.scale
import androidx.exifinterface.media.ExifInterface
import java.io.File
import kotlin.math.min

// Use Bitmap directly on Android
actual typealias KmpImage = Bitmap

// ------------------------
// Actuals for common API
// ------------------------

actual fun writeBitmapToPath(
    absolutePath: String,
    bitmap: KmpImage,
    format: ImageFormat,
    quality: Int
) {
    val androidFormat = when (format) {
        ImageFormat.JPEG -> Bitmap.CompressFormat.JPEG
        ImageFormat.PNG  -> Bitmap.CompressFormat.PNG
        ImageFormat.WEBP -> Bitmap.CompressFormat.WEBP
    }
    File(absolutePath).outputStream().use { out ->
        bitmap.compress(androidFormat, quality, out)
        out.flush()
    }
}

actual fun KmpImage.resizeToMax(maxWidth: Int, maxHeight: Int): KmpImage {
    // No need to resize
    if (this.width == maxWidth && this.height == maxHeight) return this

    val aspectRatio = this.width.toFloat() / this.height.toFloat()
    val useWidth = aspectRatio >= 1f
    val calculatedMaxWidth = min(this.width, maxWidth)
    val calculatedMinHeight = min(this.height, maxHeight)
    val width = if (useWidth) calculatedMaxWidth else (calculatedMinHeight * aspectRatio).toInt()
    val height = if (useWidth) (calculatedMaxWidth / aspectRatio).toInt() else calculatedMinHeight
    return scale(width, height)
}

actual fun calculateInSampleSize(
    outWidth: Int,
    outHeight: Int,
    desiredWidth: Int,
    desiredHeight: Int
): Int {
    var inSampleSize = 1
    if (outWidth > desiredWidth || outHeight > desiredHeight) {
        val halfHeight = outHeight / 2
        val halfWidth = outWidth / 2
        while (halfHeight / inSampleSize >= desiredHeight && halfWidth / inSampleSize >= desiredWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}

actual fun decodeImageWithSample(
    bytes: ByteArray,
    desiredWidth: Int,
    desiredHeight: Int
): KmpImage {
    val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
    opts.inJustDecodeBounds = false
    opts.inSampleSize = calculateInSampleSize(
        outWidth = opts.outWidth,
        outHeight = opts.outHeight,
        desiredWidth = desiredWidth,
        desiredHeight = desiredHeight
    )
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
}

private fun ExifOrientation.toAndroid(): Int = when (this) {
    ExifOrientation.ORIENTATION_ROTATE_270 -> ExifInterface.ORIENTATION_ROTATE_270
    ExifOrientation.ORIENTATION_ROTATE_180 -> ExifInterface.ORIENTATION_ROTATE_180
    ExifOrientation.ORIENTATION_ROTATE_90  -> ExifInterface.ORIENTATION_ROTATE_90
    ExifOrientation.ORIENTATION_FLIP_HORIZONTAL -> ExifInterface.ORIENTATION_FLIP_HORIZONTAL
    ExifOrientation.ORIENTATION_FLIP_VERTICAL   -> ExifInterface.ORIENTATION_FLIP_VERTICAL
    ExifOrientation.ORIENTATION_TRANSPOSE       -> ExifInterface.ORIENTATION_TRANSPOSE
    ExifOrientation.ORIENTATION_TRANSVERSE      -> ExifInterface.ORIENTATION_TRANSVERSE
    else -> ExifInterface.ORIENTATION_NORMAL
}

actual fun KmpImage.rotateToExifMetadataOrientation(
    orientation: ExifOrientation
): KmpImage {
    val matrix = Matrix()
    when (orientation.toAndroid()) {
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_90  -> matrix.postRotate(90f)
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL   -> matrix.preScale(1f, -1f)
        ExifInterface.ORIENTATION_TRANSPOSE -> {
            matrix.preRotate(-90f)
            matrix.preScale(-1f, 1f)
        }
        ExifInterface.ORIENTATION_TRANSVERSE -> {
            matrix.preRotate(90f)
            matrix.preScale(-1f, 1f)
        }
        else -> return this
    }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

// -----------------------------------------------------------
// Android-only shims to keep your ORIGINAL names working too
// (So existing Android code compiles unchanged.)
// -----------------------------------------------------------

/** Original: File.writeBitmap(bitmap, format, quality) */
fun File.writeBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int) {
    outputStream().use { out ->
        bitmap.compress(format, quality, out)
        out.flush()
    }
}

/** Original: BitmapFactory.Options.calculateInSampleSize(...) */
fun BitmapFactory.Options.calculateInSampleSize(
    desiredWidth: Int,
    desiredHeight: Int
): Int = calculateInSampleSize(outWidth, outHeight, desiredWidth, desiredHeight)

/** Original: Bitmap.rotateToExifMetadataOrientation(ExifInterface.ORIENTATION_*) */
fun Bitmap.rotateToExifMetadataOrientation(orientation: Int): Bitmap {
    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_90  -> matrix.postRotate(90f)
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL   -> matrix.preScale(1f, -1f)
        ExifInterface.ORIENTATION_TRANSPOSE -> {
            matrix.preRotate(-90f); matrix.preScale(-1f, 1f)
        }
        ExifInterface.ORIENTATION_TRANSVERSE -> {
            matrix.preRotate(90f); matrix.preScale(-1f, 1f)
        }
        else -> return this
    }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}