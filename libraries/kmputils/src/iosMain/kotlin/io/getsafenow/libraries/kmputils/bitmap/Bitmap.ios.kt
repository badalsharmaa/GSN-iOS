@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.getsafenow.libraries.kmputils.bitmap

import kotlinx.cinterop.CValue
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.CGAffineTransform
import platform.CoreGraphics.CGAffineTransformConcat
import platform.CoreGraphics.CGAffineTransformMake
import platform.CoreGraphics.CGAffineTransformMakeRotation
import platform.CoreGraphics.CGAffineTransformMakeScale
import platform.CoreGraphics.CGContextConcatCTM
import platform.CoreGraphics.CGContextDrawImage
import platform.CoreGraphics.CGContextRef
import platform.CoreGraphics.CGContextTranslateCTM
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGRectApplyAffineTransform
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSize
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithBytes
import platform.Foundation.writeToURL
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetCurrentContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePNGRepresentation
import kotlin.math.min

// ------------------------
// Platform image type
// ------------------------
actual typealias KmpImage = UIImage

// ------------------------
// Helpers
// ------------------------

private fun UIImage.toPngData(): NSData? = UIImagePNGRepresentation(this)
private fun UIImage.toJpegData(quality: Int): NSData? =
    UIImageJPEGRepresentation(this, (quality.coerceIn(0, 100) / 100.0))

private fun nsDataFrom(bytes: ByteArray): NSData =
    bytes.usePinned { pinned -> NSData.dataWithBytes(pinned.addressOf(0), bytes.size.toULong()) }

private fun writeNSDataToPath(data: NSData, absolutePath: String) {
    val url = NSURL.fileURLWithPath(absolutePath)
    data.writeToURL(url, true)
}

/** aspect-fit for CGSize (use CValue<CGSize> in K/N) */
private fun CValue<CGSize>.aspectFit(maxWidth: Int, maxHeight: Int): CValue<CGSize> = useContents {
    val w = width
    val h = height
    if (w <= 0.0 || h <= 0.0) return CGSizeMake(0.0, 0.0)

    val aspect = w / h
    val targetW = min(w, maxWidth.toDouble())
    val targetH = min(h, maxHeight.toDouble())
    val useWidth = aspect >= 1.0
    val outW = if (useWidth) targetW else (targetH * aspect)
    val outH = if (useWidth) (targetW / aspect) else targetH
    CGSizeMake(outW, outH)
}

/** Draw this UIImage into a new bitmap context of size `to` */
private fun UIImage.drawScaled(to: CValue<CGSize>): UIImage {
    UIGraphicsBeginImageContextWithOptions(to, /*opaque*/ false, /*scale*/ 1.0)
    val rect = to.useContents { CGRectMake(0.0, 0.0, width, height) }
    this.drawInRect(rect)
    val out = UIGraphicsGetImageFromCurrentImageContext()!!
    UIGraphicsEndImageContext()
    return out
}

/** Map our enum to CoreGraphics transform (CValue<CGAffineTransform>) */
private fun ExifOrientation.toCGAffine(): CValue<CGAffineTransform> = when (this) {
    ExifOrientation.ORIENTATION_ROTATE_90  -> CGAffineTransformMakeRotation((90.0  * kotlin.math.PI) / 180.0)
    ExifOrientation.ORIENTATION_ROTATE_180 -> CGAffineTransformMakeRotation((180.0 * kotlin.math.PI) / 180.0)
    ExifOrientation.ORIENTATION_ROTATE_270 -> CGAffineTransformMakeRotation((270.0 * kotlin.math.PI) / 180.0)
    ExifOrientation.ORIENTATION_FLIP_HORIZONTAL -> CGAffineTransformMakeScale(-1.0, 1.0)
    ExifOrientation.ORIENTATION_FLIP_VERTICAL   -> CGAffineTransformMakeScale( 1.0,-1.0)
    ExifOrientation.ORIENTATION_TRANSPOSE -> CGAffineTransformConcat(
        CGAffineTransformMakeRotation((-90.0 * kotlin.math.PI) / 180.0),
        CGAffineTransformMakeScale(-1.0, 1.0)
    )
    ExifOrientation.ORIENTATION_TRANSVERSE -> CGAffineTransformConcat(
        CGAffineTransformMakeRotation((90.0 * kotlin.math.PI) / 180.0),
        CGAffineTransformMakeScale(-1.0, 1.0)
    )
    else -> CGAffineTransformMake(1.0, 0.0, 0.0, 1.0, 0.0, 0.0) // identity as CValue
}


/**
 * Apply an affine transform to the image.
 * IMPORTANT: Use CValue<CGAffineTransform> and read CGSize via useContents{}.
 */
private fun UIImage.applyTransform(transform: CValue<CGAffineTransform>): UIImage {
    val srcSize: CValue<CGSize> = this.size
    val transformedRect: CValue<CGRect> = srcSize.useContents {
        val srcRect = CGRectMake(0.0, 0.0, width, height)
        CGRectApplyAffineTransform(srcRect, transform)
    }
    val dstW = kotlin.math.abs(transformedRect.useContents { size.width })
    val dstH = kotlin.math.abs(transformedRect.useContents { size.height })
    val dstSize = CGSizeMake(dstW, dstH)

    UIGraphicsBeginImageContextWithOptions(dstSize, false, 1.0)
    val ctx: CGContextRef = UIGraphicsGetCurrentContext()
        ?: error("No current graphics context")

    CGContextTranslateCTM(ctx, dstW / 2.0, dstH / 2.0)
    CGContextConcatCTM(ctx, transform)

    val drawRect = srcSize.useContents {
        CGRectMake(-width / 2.0, -height / 2.0, width, height)
    }

    this.CGImage?.let { cg ->
        CGContextDrawImage(ctx, drawRect, cg)
    } ?: run {
        this.drawInRect(drawRect)
    }

    val out = UIGraphicsGetImageFromCurrentImageContext()
        ?: error("Failed to get image from context")
    UIGraphicsEndImageContext()
    return out
}


// ------------------------
// Actual's for common API
// ------------------------

actual fun writeBitmapToPath(
    absolutePath: String,
    bitmap: KmpImage,
    format: ImageFormat,
    quality: Int
) {
    val data: NSData? = when (format) {
        ImageFormat.PNG  -> bitmap.toPngData()
        ImageFormat.JPEG -> bitmap.toJpegData(quality)
        ImageFormat.WEBP -> bitmap.toPngData() // iOS has no native WEBP encoder; fallback to PNG
    }
    requireNotNull(data) { "Failed to encode image" }
    writeNSDataToPath(data, absolutePath)
}

actual fun KmpImage.resizeToMax(maxWidth: Int, maxHeight: Int): KmpImage {
    require(maxWidth > 0 && maxHeight > 0) { "maxWidth/maxHeight must be > 0" }

    val withinBounds = this.size.useContents {
        width <= maxWidth.toDouble() && height <= maxHeight.toDouble()
    }
    if (withinBounds) return this

    val target: CValue<CGSize> = this.size.aspectFit(maxWidth, maxHeight)
    return this.drawScaled(target)
}

actual fun calculateInSampleSize(
    outWidth: Int,
    outHeight: Int,
    desiredWidth: Int,
    desiredHeight: Int
): Int {
    var inSampleSize = 1
    if (outWidth > desiredWidth || outHeight > desiredHeight) {
        var halfH = outHeight / 2
        var halfW = outWidth / 2
        while (halfH / inSampleSize >= desiredHeight && halfW / inSampleSize >= desiredWidth) {
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
    val data = nsDataFrom(bytes)
    val img = UIImage(data = data) ?: error("Failed to decode image bytes")
    return img.resizeToMax(desiredWidth, desiredHeight)
}

actual fun KmpImage.rotateToExifMetadataOrientation(
    orientation: ExifOrientation
): KmpImage {
    if (orientation == ExifOrientation.ORIENTATION_NORMAL ||
        orientation == ExifOrientation.ORIENTATION_UNDEFINED) {
        return this
    }
    val t: CValue<CGAffineTransform> = orientation.toCGAffine()
    return this.applyTransform(t)
}
