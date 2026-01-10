package com.example.getsafenowclient.call.webrtc

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shepeliev.webrtckmp.MediaStream

@Composable
expect fun VideoRenderer(
    stream: MediaStream,
    modifier: Modifier = Modifier,
    isMirror: Boolean = false
)
