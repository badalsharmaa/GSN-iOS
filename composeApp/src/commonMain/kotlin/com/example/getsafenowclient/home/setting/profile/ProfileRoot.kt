package com.example.getsafenowclient.home.setting.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.arkivanov.decompose.ComponentContext
import com.example.getsafenowclient.service.SessionManager
import net.folivo.trixnity.client.MatrixClient

@Composable
fun ProfileRoot(
    componentContext: ComponentContext,
    client: MatrixClient,
    sessionManager: SessionManager,
    onBack: () -> Unit,
    onClose: () -> Unit,
    onSignOutComplete: () -> Unit
) {
    val profileComponent = remember(componentContext) {
        ProfileScreenModel(componentContext, client)
    }

    ProfileScreen(
        profileComponent = profileComponent,
        sessionManager = sessionManager,
        onBack = onBack,
        onClose = onClose,
        onSignOutComplete = onSignOutComplete
    )
}
