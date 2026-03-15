package com.example.getsafenowclient.home

import androidx.compose.runtime.Composable
import co.touchlab.kermit.Logger
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.example.getsafenowclient.common.events.membership.StateEventResolver
import com.example.getsafenowclient.common.events.message.MessageEventFormatter
import com.example.getsafenowclient.matrixentensions.dmRoomToPeerMapFlow
import com.example.getsafenowclient.utils.CallUtils.isVideoOffer
import com.example.getsafenowclient.utils.getSubTree
import com.example.getsafenowclient.utils.isRoot
import com.example.getsafenowclient.utils.isSpace
import com.example.getsafenowclient.utils.nameFlow
import io.getsafenow.libraries.architecture.ScreenComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.client.flatten
import net.folivo.trixnity.client.room
import net.folivo.trixnity.client.store.Room
import net.folivo.trixnity.client.store.RoomUser
import net.folivo.trixnity.client.store.eventId
import net.folivo.trixnity.client.store.sender
import net.folivo.trixnity.client.user
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.UserId
import net.folivo.trixnity.core.model.events.ClientEvent
import net.folivo.trixnity.core.model.events.m.call.CallEventContent
import net.folivo.trixnity.core.model.events.m.room.MemberEventContent
import net.folivo.trixnity.core.model.events.m.room.Membership
import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private val logger = Logger.withTag("HomeViewComponent")

interface HomeViewComponent : ScreenComponent {
    val chats: Flow<List<RoomHeader>>
    val catalog: Flow<List<RoomHeader>>
    val invites: Flow<List<RoomHeader>>
}

@OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class)
class HomeViewScreenModel(
    componentContext: ComponentContext,
    private val client: MatrixClient
) :  HomeViewComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate).also { sc ->
        lifecycle.doOnDestroy { sc.cancel() }
    }

    private val chatsState = MutableStateFlow<List<RoomHeader>>(emptyList())
    override val chats: Flow<List<RoomHeader>> = chatsState

    private val catalogState = MutableStateFlow<List<RoomHeader>>(emptyList())
    override val catalog: Flow<List<RoomHeader>> = catalogState

    private val invitesState = MutableStateFlow<List<RoomHeader>>(emptyList())
    override val invites: Flow<List<RoomHeader>> = invitesState

    private val userStateCache = mutableMapOf<Pair<RoomId, UserId>, StateFlow<RoomUser?>>()


    init {
        val roomService = client.room

        // Aggregate all room headers as a hot shared stream. Now includes JOIN and INVITE.
        val allHeaders: StateFlow<List<Pair<Membership, RoomHeader>>> =
            client.room.getAll()
                .flatten()
                .map { map -> map.values.filterNotNull() }   // list<Room>
                .flatMapLatest { rooms ->
                    // 🔍 DEBUG: Log raw rooms from database
                    logger.d { "🗄️ Raw rooms from DB: ${rooms.size}" }
                    rooms.forEach { room ->
                        logger.d { "  DB Room: ${room.roomId.full} -> Membership: ${room.membership}" }
                    }
                    
                    if (rooms.isEmpty()) {
                        MutableStateFlow(emptyList())
                    } else {
                        // Create header flows for each room
                        val flows = rooms.map { room ->
                            headerFlow(room, client).map { h -> room.membership to h }
                        }

                        // Combine only when a header actually changes
                        combine(flows) { it.toList() }
                    }
                }
                .distinctUntilChanged()
                .stateIn(scope, SharingStarted.WhileSubscribed(), emptyList())

        // 🔍 DEBUG: Log all rooms and their membership status
        scope.launch {
            allHeaders.collect { headers ->
                logger.d { "📦 Total rooms: ${headers.size}" }
                headers.forEach { (membership, header) ->
                    logger.d { "  Room: ${header.title} (${header.id.full}) -> Membership: $membership" }
                }
                val inviteCount = headers.count { it.first == Membership.INVITE }
                logger.d { "📨 Invite rooms found: $inviteCount" }
            }
        }

        // Invites Flow
        val invitesFlow = allHeaders.map { headersWithMembership ->
            headersWithMembership
                .filter { (membership, _) -> membership == Membership.INVITE }
                .map { (_, header) -> header }
                .sortedByDescending { it.lastMessageDate }
        }

        // Chats (non-space rooms)
        val chatsFlow = combine(allHeaders, client.dmRoomToPeerMapFlow()) { headersWithMembership, dmMap ->
            val joinHeaders = headersWithMembership
                .filter { (membership, _) -> membership == Membership.JOIN }
                .map { (_, header) -> header }

            joinHeaders
                // only non-space rooms
                .filter { !it.isSpace && roomService.isRoot(it.id) }
                // group by userId (for DMs) or roomId (for normal)
                .groupBy { dmMap[it.id] ?: it.id }
                // keep only the most recent room per user/group
                .values.mapNotNull { group ->
                    group.maxByOrNull { it.lastMessageDate }
                }
                .sortedByDescending { it.lastMessageDate }
        }

        // Catalog (spaces + subtree)
        val catalogFlow = allHeaders.map { headersWithMembership ->
            val joinHeaders = headersWithMembership
                .filter { (membership, _) -> membership == Membership.JOIN }
                .map { (_, header) -> header }

            val sortedCatalog = mutableListOf<RoomId>()
            val catalogRoots = joinHeaders.filter { it.isSpace && roomService.isRoot(it.id) }
            catalogRoots.forEach { root ->
                sortedCatalog.add(root.id)
                sortedCatalog.addAll(roomService.getSubTree(root.id))
            }
            val index = joinHeaders.associateBy { it.id }
            sortedCatalog.mapNotNull { index[it] }
        }

        scope.launch { chatsFlow.collect { chatsState.value = it } }
        scope.launch { catalogFlow.collect { catalogState.value = it } }
        scope.launch { 
            invitesFlow.collect { invites ->
                logger.d { "📨 Invites flow updated: count=${invites.size}" }
                invites.forEach { invite ->
                    logger.d { "  - ${invite.title} (${invite.id.full})" }
                }
                invitesState.value = invites
            } 
        }
    }

    // -------------------------------------------------------------------------
    // Room Header Flow
    // -------------------------------------------------------------------------
    @OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class)
    private fun headerFlow(room: Room, client: MatrixClient): Flow<RoomHeader> {

        // initial header (static)
        val initial = RoomHeader(
            id = room.roomId,
            title = "",
            lastMessageText = "",
            lastMessageDate = room.lastRelevantEventTimestamp
                ?: Instant.fromEpochMilliseconds(0),
            unreadCount = room.unreadMessageCount,
            avatarUrl = room.avatarUrl,
            isSpace = false
        )

        // room name changes
        val titleFlow = room.nameFlow(client)
            .distinctUntilChanged()

        // unread count updates
        val unreadFlow = client.room.getById(room.roomId)
            .filterNotNull()
            .map { it.unreadMessageCount }
            .distinctUntilChanged()

        // last event (sender + timestamp + content)
        val lastEventFlow = client.room
            .getLastTimelineEvent(room.roomId)
            .filterNotNull()
            .flatMapLatest { it }   // timeline event inside a wrapper
            .filterNotNull()
            .distinctUntilChanged { old, new ->
                old.eventId == new.eventId  // last event change
            }

        // user of last event
        val userFlow = lastEventFlow
            .flatMapLatest { client.user.getById(room.roomId, it.sender) }
            .distinctUntilChanged()

        // last event timestamp
        val dateFlow = lastEventFlow
            .map { Instant.fromEpochMilliseconds(it.event.originTimestamp) }
            .distinctUntilChanged()

        // last event message text
        val messageFlow = lastEventFlow.map { timelineEvent ->
            val content = timelineEvent.content?.getOrNull()

            when (content) {

                // ---------------------------------------------------------
                // 1) TEXT / MEDIA MESSAGE
                // ---------------------------------------------------------
                is RoomMessageEventContent -> MessageEventFormatter.formatPreview(content)

                // ---------------------------------------------------------
                // 2) CALL EVENTS (Invite / Answer / Hangup / Candidates)
                // Use unified CallStateResolver for consistent states
                // ---------------------------------------------------------
                is CallEventContent.Invite,
                is CallEventContent.Answer,
                is CallEventContent.Candidates,
                is CallEventContent.Hangup -> {
                    val callId = com.example.getsafenowclient.utils.CallStateResolver.getCallId(content)
                    
                    if (callId != null) {
                        // Fetch recent timeline events to analyze the full call sequence
                        val room = client.room.getById(room.roomId).first()
                        val lastEventId = room?.lastEventId
                        
                        if (lastEventId != null) {
                            val recentEvents = client.room.getTimelineEvents(
                                roomId = room.roomId,
                                startFrom = lastEventId,
                                direction = net.folivo.trixnity.clientserverapi.model.rooms.GetEvents.Direction.BACKWARDS
                            ) {
                                maxSize = 100
                                decryptionTimeout = kotlin.time.Duration.ZERO
                            }.map { it.first() }.take(100).toList()
                            
                            // Use unified CallStateResolver
                            val callState = com.example.getsafenowclient.utils.CallStateResolver.resolveCallState(
                                events = recentEvents,
                                callId = callId,
                                currentUserId = client.userId
                            )
                            callState.previewText
                        } else {
                            "[unknown call]"
                        }
                    } else {
                        "[unknown call]"
                    }
                }

                // ---------------------------------------------------------
                // 3) MEMBERSHIP EVENT
                // ---------------------------------------------------------
                is MemberEventContent -> {
                    val stateEvent = timelineEvent.event as? ClientEvent.RoomEvent.StateEvent<*>

                    val targetId = stateEvent?.stateKey?.let { UserId(it) }
                        ?: timelineEvent.event.sender

                    val senderUser = getCachedUserState(room.roomId, timelineEvent.event.sender).value
                    val targetUser = getCachedUserState(room.roomId, targetId).value

                    StateEventResolver.resolve(
                        roomId = room.roomId,
                        timelineEvent = timelineEvent,
                        currentUserId = client.userId,
                        senderUser = senderUser,
                        targetUser = targetUser
                    ) ?: "[event]"
                }

                // ---------------------------------------------------------
                // 4) UNKNOWN EVENTS
                // ---------------------------------------------------------
                else -> "[unknown]"
            }
        }.distinctUntilChanged()


        // Combine all flows into a RoomHeader
        return combine(titleFlow, messageFlow, dateFlow, unreadFlow) { title, msgText, msgDate, unread ->
            initial.copy(
                title = title,
                lastMessageText = msgText,
                lastMessageDate = msgDate,
                unreadCount = unread
            )
        }
            .distinctUntilChanged { old, new ->
                old.title == new.title &&
                        old.lastMessageText == new.lastMessageText &&
                        old.lastMessageDate == new.lastMessageDate &&
                        old.unreadCount == new.unreadCount
            }
    }


    // ---------------------------------------------------------
    // Cache user profiles as StateFlows so .value works
    // ---------------------------------------------------------
    private fun getCachedUserState(roomId: RoomId, userId: UserId): StateFlow<RoomUser?> {
        return userStateCache.getOrPut(roomId to userId) {
            client.user.getById(roomId, userId)
                .stateIn(scope, SharingStarted.Eagerly, null)
        }
    }

    @Composable
    override fun Render() {
        // Intentionally empty: this is a headless component exposing flows for a parent screen.
        // Use chats/catalog in your Home UI composable via collectAsState().
    }
}
