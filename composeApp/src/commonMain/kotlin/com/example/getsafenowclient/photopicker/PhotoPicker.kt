package com.example.getsafenowclient.photopicker

import androidx.compose.runtime.Composable

@Composable
expect fun rememberPhotoPickerLauncher(
    onResult: (ByteArray?) -> Unit
): PhotoPickerLauncher

interface PhotoPickerLauncher {
    fun launch()
}
