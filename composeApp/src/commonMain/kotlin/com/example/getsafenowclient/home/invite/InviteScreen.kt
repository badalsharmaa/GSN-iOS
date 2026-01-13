package com.example.getsafenowclient.home.invite

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.getsafenowclient.component.InviteList
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ArrowLeft
import net.folivo.trixnity.client.MatrixClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteScreen(
    state: InviteState,
    client: MatrixClient
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invites") },
                navigationIcon = {
                    IconButton(onClick = { state.eventSink(InviteEvent.Close) }) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.ArrowLeft,
                            contentDescription = "Back",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // This padding is crucial
        ) {
            InviteList(
                invites = state.invites,
                client = client,
                onAcceptClick = { state.eventSink(InviteEvent.AcceptInvite(it)) },
                onDeclineClick = { state.eventSink(InviteEvent.DeclineInvite(it)) }
            )
        }
    }
}
