package com.example.getsafenowclient.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.getsafenowclient.component.ConversationListItemData
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.clientserverapi.client.SyncState
import net.folivo.trixnity.core.model.RoomId
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * The presenter for the new home screen. It manages the screen's state and logic.
 */
@OptIn(ExperimentalTime::class)
@Composable
fun homePresenter(
    homeViewScreenModel: HomeViewScreenModel,
    client: MatrixClient,
    onOpenRoom: (RoomId) -> Unit,
    onOpenInvites: () -> Unit,
    onOpenSettings: () -> Unit
): Pair<HomeState, (HomeEvent) -> Unit> {

    var searchQuery by remember { mutableStateOf("") }

    val homeData by produceState<HomeData>(initialValue = HomeData()) { 
        val profileFlow = combine(client.displayName, client.avatarUrl) { displayName, avatarUrl ->
            ProfileData(
                userId = client.userId.full,
                userName = displayName ?: client.userId.localpart,
                userAvatarUrl = avatarUrl
            )
        }

        val conversationsFlow = homeViewScreenModel.chats.map { roomHeaders ->
            roomHeaders.map { roomHeader ->
                ConversationListItemData(
                    id = roomHeader.id.full,
                    name = roomHeader.title,
                    avatarUrl = roomHeader.avatarUrl?.toString(),
                    lastMessage = roomHeader.lastMessageText,
                    timestamp = formatTimestamp(roomHeader.lastMessageDate),
                    unreadCount = roomHeader.unreadCount.toInt(),
                    isFavorited = false // Placeholder for favorite status
                )
            }
        }
        
        val invitesFlow = homeViewScreenModel.invites.map { it.isNotEmpty() }

        combine(profileFlow, conversationsFlow, invitesFlow) { profile, conversations, hasInvites ->
            HomeData(profile, conversations, hasInvites)
        }.collect { value = it }
    }

    val (profile, allConversations, hasNewInvites) = homeData
    val syncState by client.syncState.collectAsState()

    val filteredConversations = remember(allConversations, searchQuery) {
        if (searchQuery.isBlank()) {
            allConversations
        } else {
            allConversations.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.lastMessage.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val eventSink: (HomeEvent) -> Unit = remember(onOpenRoom, onOpenInvites, onOpenSettings) { {
        event ->
            when (event) {
                is HomeEvent.ConversationClicked -> onOpenRoom(RoomId(event.id))
                is HomeEvent.UpdateSearchQuery -> searchQuery = event.query
                is HomeEvent.InviteClicked -> onOpenInvites()
                HomeEvent.AvatarClicked -> onOpenSettings()
                // TODO: Handle other events like FilterClicked, FavoriteClicked
                else -> {}
            }
        }
    }

    val state = HomeState(
        userId = profile?.userId ?: "",
        userName = profile?.userName ?: "",
        userAvatarUrl = profile?.userAvatarUrl,
        conversations = filteredConversations,
        isLoading = syncState == SyncState.INITIAL_SYNC || profile == null,
        searchQuery = searchQuery,
        hasNewInvites = hasNewInvites
    )

    return state to eventSink
}

private data class HomeData(
    val profile: ProfileData? = null,
    val conversations: List<ConversationListItemData> = emptyList(),
    val hasNewInvites: Boolean = false
)

private data class ProfileData(
    val userId: String,
    val userName: String,
    val userAvatarUrl: String?
)

@OptIn(ExperimentalTime::class)
private fun formatTimestamp(instant: Instant): String {
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val month = localDateTime.month.name.substring(0, 3).lowercase().replaceFirstChar { it.uppercaseChar() }
    return "$month ${localDateTime.day}"
}
