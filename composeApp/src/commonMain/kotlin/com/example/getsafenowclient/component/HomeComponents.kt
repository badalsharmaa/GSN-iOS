package com.example.getsafenowclient.component

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.getsafenowclient.common.ui.GsnAvatarAdvanced
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Regular
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.regular.Square
import compose.icons.fontawesomeicons.regular.Star
import compose.icons.fontawesomeicons.solid.Comments
import compose.icons.fontawesomeicons.solid.DoorOpen
import compose.icons.fontawesomeicons.solid.Filter
import compose.icons.fontawesomeicons.solid.Pen
import compose.icons.fontawesomeicons.solid.Search
import compose.icons.fontawesomeicons.solid.Star
import io.getsafenow.libraries.designcomponents.utils.preview.GsnPreview
import io.getsafenow.libraries.gsn_theme.customtheme.GsnTheme
import net.folivo.trixnity.client.MatrixClient
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun HomeHeader(
    modifier: Modifier = Modifier,
    client: MatrixClient?,
    userId: String,
    userName: String,
    userAvatarUrl: String?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    onAvatarClick: () -> Unit,
    hasNewInvites: Boolean,
    onInviteClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top row: Avatar + Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(onClick = onAvatarClick) {
                GsnAvatarAdvanced(
                    modifier = Modifier.size(40.dp),
                    id = userId,
                    name = userName,
                    url = userAvatarUrl,
                    client = client
                )
            }
            Text(
                text = "Messages",
                style = GsnTheme.materialTypography.headlineSmall,
                color = GsnTheme.colors.textPrimary
            )
        }

        // Bottom row: Search bar + Filter
        ConversationSearchBar(
            query = searchQuery,
            onQueryChange = onSearchQueryChange,
            onFilterClick = onFilterClick
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            InviteButton(
                hasNewInvites = hasNewInvites,
                onClick = onInviteClick
            )
        }
    }
}

@Composable
private fun ConversationSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            modifier = Modifier
                .weight(1f)
                .height(55.dp),
            value = query,
            onValueChange = onQueryChange,
            placeholder = {
                Text(
                    text = "Search conversations...",
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    style = GsnTheme.typography.fontBodyLgMedium,
                    color = GsnTheme.colors.textSecondary,
                    modifier = Modifier.basicMarquee()
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
                unfocusedTextColor = GsnTheme.colors.textPrimary,
                focusedLeadingIconColor = GsnTheme.colors.iconSecondary,
                unfocusedLeadingIconColor = GsnTheme.colors.iconSecondary
            ),
            textStyle = GsnTheme.typography.fontBodyLgRegular,
            singleLine = true,
            maxLines = 1
        )
        IconButton(onClick = onFilterClick) {
            Icon(
                imageVector = FontAwesomeIcons.Solid.Filter,
                contentDescription = "Sort or Filter",
                tint = GsnTheme.colors.iconPrimary,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@Composable
fun InviteButton(
    modifier: Modifier = Modifier,
    hasNewInvites: Boolean,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Invites",
                style = GsnTheme.typography.fontBodyLgMedium,
                color = GsnTheme.colors.textPrimary
            )
            if (hasNewInvites) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(GsnTheme.colors.bgAccentRest)
                )
            }
        }
    }
}


// --- Conversation List Components ---

/**
 * Data class representing a single item in the conversation list.
 */
data class ConversationListItemData(
    val id: String,
    val name: String,
    val avatarUrl: String?,
    val lastMessage: String,
    val timestamp: String,
    val unreadCount: Int,
    val isFavorited: Boolean
)

@Composable
fun ConversationList(
    modifier: Modifier = Modifier,
    conversations: List<ConversationListItemData>,
    client: MatrixClient?,
    onItemClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit,
) {
    val uniqueRooms = remember(conversations) { conversations.distinctBy { it.id } }
    LazyColumn(modifier = modifier.fillMaxWidth()) {
        itemsIndexed(uniqueRooms, key = { index, item -> "room:${item.id}#$index" }) { _, item ->
            ConversationItem(
                client = client,
                itemData = item,
                onItemClick = { onItemClick(item.id) },
                onFavoriteClick = { onFavoriteClick(item.id) }
            )
            HorizontalDivider(color = GsnTheme.colors.borderInteractiveSecondary, thickness = 1.dp)
        }
    }
}

@Composable
fun ConversationItem(
    modifier: Modifier = Modifier,
    client: MatrixClient?,
    itemData: ConversationListItemData,
    onItemClick: () -> Unit,
    onFavoriteClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        GsnAvatarAdvanced(
            modifier = Modifier.size(56.dp),
            id = itemData.id,
            name = itemData.name,
            url = itemData.avatarUrl,
            client = client
        )

        Spacer(Modifier.width(16.dp))

        // Name and Message
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = itemData.name,
                style = GsnTheme.typography.fontBodyLgMedium.copy(fontWeight = FontWeight.SemiBold),
                color = GsnTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = itemData.lastMessage,
                style = GsnTheme.typography.fontBodyMdMedium,
                color = GsnTheme.colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.width(16.dp))

        // Timestamp, Unread Badge, and Favorite
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = itemData.timestamp,
                style = GsnTheme.typography.fontBodySmMedium,
                color = GsnTheme.colors.textSecondary
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (itemData.unreadCount > 0) {
                    UnreadBadge(count = itemData.unreadCount)
                }
                IconButton(
                    modifier = Modifier.size(24.dp),
                    onClick = onFavoriteClick
                ) {
                    val starIcon = if (itemData.isFavorited) FontAwesomeIcons.Solid.Star else FontAwesomeIcons.Regular.Star
                    val starTint = if (itemData.isFavorited) GsnTheme.colors.bgAccentRest else GsnTheme.colors.iconSecondary
                    Icon(
                        imageVector = starIcon,
                        contentDescription = if (itemData.isFavorited) "Unfavorite" else "Favorite",
                        tint = starTint,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun UnreadBadge(
    count: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(GsnTheme.colors.bgAccentRest),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = count.toString(),
            color = GsnTheme.colors.textOnSolidPrimary,
            style = GsnTheme.typography.fontBodyXsMedium,
            maxLines = 1
        )
    }
}

// --- Floating Action Button ---

@Composable
fun HomeFloatingActionButton(
    modifier: Modifier = Modifier,
    onStartNewChatClick: () -> Unit,
    onCreateRoomClick: () -> Unit,
    onNewGroupChatClick: () -> Unit,
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        FloatingActionButton(
            onClick = { isMenuExpanded = !isMenuExpanded },
            containerColor = GsnTheme.colors.bgActionPrimaryRest,
            contentColor = GsnTheme.colors.iconOnSolidPrimary,
            shape = RoundedCornerShape(16.dp),
        ) {
            Icon(
                imageVector = FontAwesomeIcons.Regular.Square,
                contentDescription = "Compose",
                modifier = Modifier.size(24.dp)
            )
        }

        DropdownMenu(
            expanded = isMenuExpanded,
            containerColor = GsnTheme.colors.bgCanvasDefaultLevel1,
            onDismissRequest = { isMenuExpanded = false },
            offset = DpOffset(0.dp, (-12).dp),
            modifier = Modifier.background(GsnTheme.colors.bgSubtleSecondary)
        ) {
            DropdownMenuItem(
                text = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.Pen,
                            contentDescription = null,
                            tint = GsnTheme.colors.iconPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Start New Chat",
                            color = GsnTheme.colors.textPrimary,
                            style = GsnTheme.typography.fontBodyLgMedium
                        )
                    }
                },
                onClick = {
                    onStartNewChatClick()
                    isMenuExpanded = false
                },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
            )
            DropdownMenuItem(
                text = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.DoorOpen,
                            contentDescription = null,
                            tint = GsnTheme.colors.iconPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Create Room",
                            color = GsnTheme.colors.textPrimary,
                            style = GsnTheme.typography.fontBodyLgMedium
                        )
                    }
                },
                onClick = {
                    onCreateRoomClick()
                    isMenuExpanded = false
                },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
            )
            DropdownMenuItem(
                text = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.Comments,
                            contentDescription = null,
                            tint = GsnTheme.colors.iconPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "New Group Chat",
                            color = GsnTheme.colors.textPrimary,
                            style = GsnTheme.typography.fontBodyLgMedium
                        )
                    }
                },
                onClick = {
                    onNewGroupChatClick()
                    isMenuExpanded = false
                },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
            )
        }
    }
}


// --- Previews ---

@Preview
@Composable
private fun HomeHeaderPreview() {
    var query by remember { mutableStateOf("") }
    GsnPreview {
        HomeHeader(
            client = null,
            userId = "previewuser",
            userName = "Preview User",
            userAvatarUrl = null,
            searchQuery = query,
            onSearchQueryChange = { query = it },
            onFilterClick = {},
            onAvatarClick = {},
            hasNewInvites = true,
            onInviteClick = {}
        )
    }
}

@Preview
@Composable
private fun ConversationListPreview() {
    val sampleConversations = remember {
        listOf(
            ConversationListItemData("1", "Sarah Wilson", null, "See you tomorrow!", "Oct 29", 2, false),
            ConversationListItemData("2", "Mike Johnson", null, "Thanks for the update", "Oct 29", 0, true),
            ConversationListItemData("3", "Emma Davis", null, "Can we reschedule?", "Oct 28", 1, false),
            ConversationListItemData("4", "James Brown", null, "Perfect! 👍", "Oct 28", 0, false),
            ConversationListItemData("5", "Lisa Anderson", null, "I sent you the files", "Oct 27", 0, false),
            ConversationListItemData("6", "Very Long Name To See How Ellipsis Works", null, "This is also a very long message to check overflow.", "Oct 26", 99, true),
        )
    }
    GsnPreview {
        ConversationList(
            conversations = sampleConversations,
            client = null,
            onItemClick = {},
            onFavoriteClick = {}
        )
    }
}

@Preview
@Composable
private fun HomeFloatingActionButtonPreview() {
    GsnPreview {
        HomeFloatingActionButton(
            onStartNewChatClick = {},
            onCreateRoomClick = {},
            onNewGroupChatClick = {}
        )
    }
}

@Preview
@Composable
private fun InviteButtonPreview() {
    GsnPreview {
        Column {
            InviteButton(hasNewInvites = true, onClick = {})
            InviteButton(hasNewInvites = false, onClick = {})
        }
    }
}
