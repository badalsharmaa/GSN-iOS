package com.example.getsafenowclient.home.invite

import androidx.compose.runtime.Composable
import com.example.getsafenowclient.home.HomeViewScreenModel
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.core.model.RoomId

@Composable
fun InviteRoute(
    homeViewScreenModel: HomeViewScreenModel,
    client: MatrixClient,
    onOpenRoom: (RoomId) -> Unit,
    onClose: () -> Unit
) {
    val (state, eventSink) = invitePresenter(
        homeViewScreenModel = homeViewScreenModel,
        client = client,
        onOpenRoom = onOpenRoom,
        onClose = onClose
    )
    InviteScreen(
        state = state,
        client = client
    )
}
