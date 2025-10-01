package com.example.getsafenowclient.designsystem_preview

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.getsafenow.libraries.designcomponents.utils.preview.GsnPreview
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun GsnPreview_Demo() {
    GsnPreview(
    ) {
        AsyncImage(
            model = "invalid",
            contentDescription = "demo",
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        )
    }
}