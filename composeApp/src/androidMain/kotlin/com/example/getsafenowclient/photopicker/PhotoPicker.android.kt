package com.example.getsafenowclient.photopicker

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.io.InputStream

@Composable
actual fun rememberPhotoPickerLauncher(
    onResult: (ByteArray?) -> Unit
): PhotoPickerLauncher {
    val context = LocalContext.current
    val contentResolver = context.contentResolver

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri == null) {
            onResult(null)
            return@rememberLauncherForActivityResult
        }
        
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            onResult(bytes)
        } catch (e: Exception) {
            e.printStackTrace()
            onResult(null)
        }
    }

    return remember {
        object : PhotoPickerLauncher {
            override fun launch() {
                launcher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        }
    }
}
