package com.example.getsafenowclient.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import coil3.compose.AsyncImage
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.client.media

@Composable
fun ThumbnailLoader(
    client: MatrixClient,
    mxcUrl: String
) {
    androidx.compose.runtime.LaunchedEffect(mxcUrl) { }
    val thumbBytes = remember(mxcUrl) { mutableStateOf<ByteArray?>(null) }

    LaunchedEffect(mxcUrl) {
        val mediaResult = client.media.getThumbnail(
            uri = mxcUrl,
            width = 300,
            height = 300
        ).getOrNull()

        thumbBytes.value = mediaResult?.toByteArray()
    }

    if (thumbBytes.value != null) {
        AsyncImage(
            model = thumbBytes.value,
            contentDescription = "Video Thumbnail",
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        )
    }
}