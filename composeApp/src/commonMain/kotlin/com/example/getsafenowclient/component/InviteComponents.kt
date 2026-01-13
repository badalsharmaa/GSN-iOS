package com.example.getsafenowclient.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.getsafenowclient.common.ui.GsnAvatarAdvanced
import io.getsafenow.libraries.designcomponents.utils.preview.GsnPreview
import io.getsafenow.libraries.gsn_theme.customtheme.GsnTheme
import net.folivo.trixnity.client.MatrixClient
import org.jetbrains.compose.ui.tooling.preview.Preview

data class InviteListItemData(
    val id: String,
    val inviterName: String,
    val inviterAvatarUrl: String?,
    val roomId: String
)

@Composable
fun InviteList(
    modifier: Modifier = Modifier,
    invites: List<InviteListItemData>,
    client: MatrixClient?,
    onAcceptClick: (String) -> Unit,
    onDeclineClick: (String) -> Unit,
) {
    LazyColumn(modifier = modifier.fillMaxWidth()) {
        items(invites, key = { it.id }) { item ->
            InviteCard(
                client = client,
                itemData = item,
                onAcceptClick = { onAcceptClick(item.roomId) },
                onDeclineClick = { onDeclineClick(item.roomId) }
            )
            HorizontalDivider(color = GsnTheme.colors.borderInteractiveSecondary, thickness = 1.dp)
        }
    }
}


@Composable
fun InviteCard(
    modifier: Modifier = Modifier,
    client: MatrixClient?,
    itemData: InviteListItemData,
    onAcceptClick: () -> Unit,
    onDeclineClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GsnAvatarAdvanced(
            modifier = Modifier.size(56.dp),
            id = itemData.id,
            name = itemData.inviterName,
            url = itemData.inviterAvatarUrl,
            client = client
        )

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(itemData.inviterName)
                }
                append(" has invited you to a room")
            })

            Spacer(Modifier.size(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onDeclineClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GsnTheme.colors.bgSubtleSecondary,
                        contentColor = GsnTheme.colors.textPrimary
                    )
                ) {
                    Text("Decline")
                }
                Button(
                    onClick = onAcceptClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GsnTheme.colors.bgActionPrimaryRest,
                        contentColor = Color.White
                    )
                ) {
                    Text("Accept")
                }
            }
        }
    }
}

@Preview
@Composable
private fun InviteListPreview() {
    val sampleInvites = remember {
        listOf(
            InviteListItemData("1", "Alice", null, "room1"),
            InviteListItemData("2", "Bob", null, "room2"),
        )
    }
    GsnPreview {
        InviteList(
            invites = sampleInvites,
            client = null,
            onAcceptClick = {},
            onDeclineClick = {}
        )
    }
}
