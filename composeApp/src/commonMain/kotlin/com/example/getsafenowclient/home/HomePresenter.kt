package com.example.getsafenowclient.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.getsafenowclient.component.ConversationListItemData
import com.example.getsafenowclient.utils.listPreviewText
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
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
    
    // Track if we've loaded data at least once to prevent blocking UI on background resume
    var hasLoadedOnce by remember { mutableStateOf(false) }

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
                    timestamp = roomHeader.lastMessageDate.listPreviewText(),
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
    
    // Mark as loaded once we have profile data
    LaunchedEffect(profile) {
        if (profile != null) {
            hasLoadedOnce = true
        }
    }

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
        // Only show blocking loading screen on first launch, not on background resume
        isLoading = !hasLoadedOnce && profile == null,
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

