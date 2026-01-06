package com.example.getsafenowclient.home.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.example.getsafenowclient.component.ConversationListItemData
import com.example.getsafenowclient.component.chat.UserSearchResult
import com.example.getsafenowclient.component.chat.UserStatus
import com.example.getsafenowclient.matrixentensions.getDirectUserForRoom
import com.example.getsafenowclient.matrixentensions.getOrCreateDirectRoom
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.UserId

@Composable
fun newChatPresenter(
    client: MatrixClient,
    initialConversations: List<ConversationListItemData>,
    onOpenRoom: (RoomId) -> Unit,
    onClose: () -> Unit,
): Pair<NewChatState, (NewChatEvent) -> Unit> {
    var state by remember { mutableStateOf(NewChatState()) }
    val scope = rememberCoroutineScope()
    var initialList by remember { mutableStateOf<List<UserSearchResult>>(emptyList()) }

    // Map the initial conversations to the format the dialog's UI expects.
    LaunchedEffect(initialConversations) {
        val mapped = initialConversations.mapNotNull { convo ->
            val roomId = RoomId(convo.id)
            val directUser = client.getDirectUserForRoom(roomId)
            directUser?.let { userId ->
                UserSearchResult(
                    userId = userId.full,
                    name = convo.name,
                    avatarUrl = convo.avatarUrl,
                    status = UserStatus.LastSeen(convo.timestamp),
                    roomId = convo.id // Pass the existing room ID
                )
            }
        }
        initialList = mapped
        state = state.copy(searchResults = mapped)
    }

    LaunchedEffect(Unit) {
        state = state.copy(searchResults = initialList)
    }

    val eventSink: (NewChatEvent) -> Unit = remember(client, onOpenRoom, onClose) { {
        event ->
            when (event) {
                is NewChatEvent.SearchQueryChanged -> {
                    val query = event.query
                    state = state.copy(searchQuery = query)

                    scope.launch {
                        delay(300) // Debounce
                        if (query != state.searchQuery) return@launch

                        if (query.isBlank()) {
                            state = state.copy(searchResults = initialList, isSearching = false)
                        } else {
                            state = state.copy(isSearching = true)
                            val results = client.api.user.searchUsers(searchTerm = query, acceptLanguage = "en",  limit = 10).getOrNull()
                            val mappedResults = results?.results?.map { user ->
                                UserSearchResult(
                                    userId = user.userId.full,
                                    name = user.displayName ?: user.userId.localpart,
                                    avatarUrl = user.avatarUrl?.toString(),
                                    status = UserStatus.LastSeen("") // Presence is not available here
                                )
                            } ?: emptyList()
                            state = state.copy(searchResults = mappedResults, isSearching = false)
                        }
                    }
                }
                is NewChatEvent.UserClicked -> {
                    scope.launch {
                        val userId = UserId(event.user.userId)
                        val roomId = client.getOrCreateDirectRoom(userId).getOrThrow()
                        onOpenRoom(roomId)
                        onClose()
                    }
                }
                NewChatEvent.CloseDialog -> onClose()
            }
        }
    }
    return state to eventSink
}
