package com.example.getsafenowclient.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.getsafenowclient.component.ConversationList
import com.example.getsafenowclient.component.HomeFloatingActionButton
import com.example.getsafenowclient.component.HomeHeader
import com.example.getsafenowclient.component.LoadingScreen
import com.example.getsafenowclient.component.chat.NewChatSheet
import com.example.getsafenowclient.component.chat.UserSearchResultList
import com.example.getsafenowclient.home.chat.NewChatEvent
import com.example.getsafenowclient.home.chat.newChatPresenter
import com.example.getsafenowclient.service.SessionManager
import io.getsafenow.libraries.gsn_theme.customtheme.GsnTheme
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.core.model.RoomId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeNewScreen(
    homeViewScreenModel: HomeViewScreenModel,
    client: MatrixClient,
    onOpenRoom: (RoomId) -> Unit,
    onOpenInvites: () -> Unit,
    onOpenSettings: () -> Unit,
    sessionManager: SessionManager,
    onRequireLogin: () -> Unit
) {
    val (homeState, homeEventSink) = homePresenter(
        homeViewScreenModel = homeViewScreenModel,
        client = client,
        onOpenRoom = onOpenRoom,
        onOpenInvites = onOpenInvites,
        onOpenSettings = onOpenSettings
    )
    var showNewChatDialog by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize(), color = GsnTheme.colors.bgCanvasDefault) {
        Scaffold(
            floatingActionButton = {
                HomeFloatingActionButton(
                    onStartNewChatClick = { showNewChatDialog = true },
                    onCreateRoomClick = { homeEventSink(HomeEvent.CreateRoomClicked) },
                    onNewGroupChatClick = { homeEventSink(HomeEvent.NewGroupChatClicked) }
                )
            },
            content = {
                if (homeState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingScreen(loadingMessage = "Loading conversations...")
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize().padding(it)) {
                        HomeHeader(
                            client = client,
                            userId = homeState.userId,
                            userName = homeState.userName,
                            userAvatarUrl = homeState.userAvatarUrl,
                            searchQuery = homeState.searchQuery,
                            onSearchQueryChange = { query -> homeEventSink(HomeEvent.UpdateSearchQuery(query)) },
                            onFilterClick = { homeEventSink(HomeEvent.FilterClicked) },
                            hasNewInvites = homeState.hasNewInvites,
                            onInviteClick = { homeEventSink(HomeEvent.InviteClicked) },
                            onAvatarClick = { homeEventSink(HomeEvent.AvatarClicked) }
                        )

                        ConversationList(
                            conversations = homeState.conversations,
                            client = client,
                            onItemClick = { id -> homeEventSink(HomeEvent.ConversationClicked(id)) },
                            onFavoriteClick = { id -> homeEventSink(HomeEvent.FavoriteClicked(id)) }
                        )
                    }
                }
            }
        )

        // --- New Chat Dialog ---
        if (showNewChatDialog) {
            val (newChatState, newChatEventSink) = newChatPresenter(
                client = client,
                initialConversations = homeState.conversations,
                onOpenRoom = onOpenRoom,
                onClose = { showNewChatDialog = false }
            )

            NewChatSheet(
                searchQuery = newChatState.searchQuery,
                onSearchQueryChange = { query -> newChatEventSink(NewChatEvent.SearchQueryChanged(query)) },
                onClose = { newChatEventSink(NewChatEvent.CloseDialog) }
            ) {
                // Always use the UserSearchResultList for a consistent UI
                UserSearchResultList(
                    users = newChatState.searchResults,
                    isLoading = newChatState.isSearching,
                    client = client,
                    modifier = Modifier.heightIn(max = 300.dp),
                    onUserClick = {
                        if (newChatState.searchQuery.isBlank()) {
                            // This is an existing conversation, so just open the room.
                            // If roomId is present, use it. Otherwise fallback to userId (which shouldn't happen for existing chats).
                            val targetId = it.roomId ?: it.userId
                            homeEventSink(HomeEvent.ConversationClicked(targetId))
                            newChatEventSink(NewChatEvent.CloseDialog)
                        } else {
                            // This is a new user search result, so trigger the create/join logic.
                            newChatEventSink(NewChatEvent.UserClicked(it))
                        }
                    }
                )
            }
        }
    }
}
