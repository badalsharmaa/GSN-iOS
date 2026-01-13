package com.example.getsafenowclient.component.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.getsafenowclient.common.ui.GsnAvatarAdvanced
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Search
import compose.icons.fontawesomeicons.solid.Times
import io.getsafenow.libraries.designcomponents.utils.preview.GsnPreview
import io.getsafenow.libraries.gsn_theme.customtheme.GsnTheme
import net.folivo.trixnity.client.MatrixClient
import org.jetbrains.compose.ui.tooling.preview.Preview

// --- State and Event Definitions for User Search ---

sealed class UserStatus {
    object Online : UserStatus()
    data class LastSeen(val timestamp: String) : UserStatus()
}

data class UserSearchResult(
    val userId: String,
    val name: String,
    val avatarUrl: String?,
    val status: UserStatus,
    val roomId: String? = null // Added field to track existing Room ID
)

// --- UI Components ---

/**
 * A flexible dialog sheet for starting new chats.
 * It provides the header and search bar, and accepts a composable for its list content.
 */
@Composable
fun NewChatSheet(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    listContent: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onClose) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(GsnTheme.colors.bgCanvasDefault)
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Start New Chat",
                    style = GsnTheme.materialTypography.headlineSmall,
                    color = GsnTheme.colors.textPrimary
                )
                IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = FontAwesomeIcons.Solid.Times,
                        contentDescription = "Close",
                        tint = GsnTheme.colors.iconSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            UserSearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Content Slot for the list
            listContent()
        }
    }
}

@Composable
fun UserSearchResultList(
    users: List<UserSearchResult>,
    isLoading: Boolean,
    onUserClick: (UserSearchResult) -> Unit,
    modifier: Modifier = Modifier,
    client: MatrixClient? // For GsnAvatarAdvanced
) {
    val uniqueUsers = remember(users) { users.distinctBy { it.userId } }
    if (isLoading) {
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = GsnTheme.colors.iconAccentPrimary)
        }
    } else {
        LazyColumn(modifier = modifier.fillMaxWidth()) {
            itemsIndexed(uniqueUsers, key = { index, u -> "user:${u.userId}#$index" }) { _, user ->
                UserSearchResultItem(
                    client = client,
                    userData = user,
                    onClick = { onUserClick(user) }
                )
                HorizontalDivider(color = GsnTheme.colors.borderInteractiveSecondary, thickness = 1.dp)
            }
        }
    }
}


@Composable
private fun UserSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = "Search users...",
                style = GsnTheme.typography.fontBodyLgRegular,
                color = GsnTheme.colors.textSecondary
            )
        },
        leadingIcon = {
            Icon(
                imageVector = FontAwesomeIcons.Solid.Search,
                contentDescription = "Search",
                tint = GsnTheme.colors.iconSecondary,
                modifier = Modifier.size(16.dp)
            )
        },
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = GsnTheme.colors.bgSubtleSecondary,
            unfocusedContainerColor = GsnTheme.colors.bgSubtleSecondary,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            cursorColor = GsnTheme.colors.iconAccentPrimary,
            focusedTextColor = GsnTheme.colors.textPrimary,
            unfocusedTextColor = GsnTheme.colors.textPrimary
        ),
        textStyle = GsnTheme.typography.fontBodyLgRegular,
        singleLine = true
    )
}

@Composable
fun UserSearchResultItem(
    client: MatrixClient?,
    userData: UserSearchResult,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            GsnAvatarAdvanced(
                modifier = Modifier.size(48.dp),
                id = userData.userId,
                name = userData.name,
                url = userData.avatarUrl,
                client = client
            )
            if (userData.status is UserStatus.Online) {
                OnlineIndicator(Modifier.align(Alignment.BottomEnd))
            }
        }

        Spacer(Modifier.width(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = userData.name,
                style = GsnTheme.materialTypography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = GsnTheme.colors.textPrimary
            )
            val statusText = when (userData.status) {
                is UserStatus.Online -> "Online"
                is UserStatus.LastSeen -> "Last seen ${userData.status.timestamp}"
            }
            val statusColor = if (userData.status is UserStatus.Online) GsnTheme.colors.textSuccessPrimary else GsnTheme.colors.textSecondary
            Text(
                text = statusText,
                style = GsnTheme.typography.fontBodyMdRegular,
                color = statusColor
            )
        }
    }
}

@Composable
private fun OnlineIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(14.dp)
            .background(GsnTheme.colors.iconAccentPrimary, CircleShape)
            .border(2.dp, GsnTheme.colors.bgCanvasDefault, CircleShape)
    )
}

@Preview
@Composable
private fun NewChatSheetPreview() {
    val sampleUsers = remember {
        listOf(
            UserSearchResult("1", "Sarah Wilson", null, UserStatus.Online),
            UserSearchResult("2", "Mike Johnson", null, UserStatus.LastSeen("Oct 29")),
        )
    }
    GsnPreview {
        NewChatSheet(
            searchQuery = "",
            onSearchQueryChange = {},
            onClose = {},
        ) {
            UserSearchResultList(
                users = sampleUsers,
                isLoading = false,
                onUserClick = {},
                client = null
            )
        }
    }
}
