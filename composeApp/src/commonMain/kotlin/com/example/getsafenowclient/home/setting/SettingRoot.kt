package com.example.getsafenowclient.home.setting

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.arkivanov.decompose.ComponentContext
import net.folivo.trixnity.client.MatrixClient

@Composable
fun SettingRoot(
    componentContext: ComponentContext,
    client: MatrixClient,
    onClose: () -> Unit,
    onOpenProfile: () -> Unit
) {
    val settingComponent = remember(componentContext) {
        SettingScreenModel(componentContext, client)
    }

    SettingScreen(
        settingComponent = settingComponent,
        client = client,
        onClose = onClose,
        onOpenProfile = onOpenProfile
    )
}
