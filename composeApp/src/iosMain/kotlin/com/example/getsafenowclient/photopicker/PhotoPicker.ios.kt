package com.example.getsafenowclient.photopicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.darwin.NSObject
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberPhotoPickerLauncher(
    onResult: (ByteArray?) -> Unit
): PhotoPickerLauncher {
    val delegate = remember {
        object : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
            override fun imagePickerController(
                picker: UIImagePickerController,
                didFinishPickingMediaWithInfo: Map<Any?, *>
            ) {
                val image = didFinishPickingMediaWithInfo["UIImagePickerControllerOriginalImage"] as? UIImage
                val imageData = image?.let { UIImageJPEGRepresentation(it, 0.8) }

                picker.dismissViewControllerAnimated(true, completion = null)

                if (imageData != null) {
                    val byteArray = ByteArray(imageData.length.toInt())
                    imageData.bytes?.let {
                        byteArray.usePinned { pinned ->
                            memcpy(pinned.addressOf(0), it, imageData.length)
                        }
                    }
                    onResult(byteArray)
                } else {
                    onResult(null)
                }
            }

            override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
                picker.dismissViewControllerAnimated(true, completion = null)
                onResult(null)
            }
        }
    }

    return remember {
        object : PhotoPickerLauncher {
            override fun launch() {
                val picker = UIImagePickerController()
                picker.sourceType =
                    UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
                picker.delegate = delegate
                
                val keyWindow = UIApplication.sharedApplication.keyWindow
                val rootViewController = keyWindow?.rootViewController
                rootViewController?.presentViewController(picker, animated = true, completion = null)
            }
        }
    }
}
