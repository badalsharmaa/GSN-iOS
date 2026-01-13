package com.example.getsafenowclient.room

import co.touchlab.kermit.Logger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.client.room
import net.folivo.trixnity.client.room.Timeline
import net.folivo.trixnity.client.store.TimelineEvent
import net.folivo.trixnity.client.user
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.events.m.room.MemberEventContent
import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent
import kotlin.concurrent.Volatile

class RoomTimelineCoordinator(
    private val roomId: RoomId,
    private val client: MatrixClient,
    private val scope: CoroutineScope,
    private val eventFilter: (TimelineEvent) -> Boolean,
    private val logger: Logger
) {

    @Volatile
    private var started = false

    @Volatile
    private var initialized = false

    // ---------------------------------------------------------
    // Timeline (history)
    // ---------------------------------------------------------
    private val timeline: Timeline<TimelineEvent?> =
        client.room.getTimeline(transformer = { it.first() })

    // ---------------------------------------------------------
    // Initial load state
    // ---------------------------------------------------------
    private val _isInitialLoadComplete = MutableStateFlow(false)
    val isInitialLoadComplete: StateFlow<Boolean> =
        _isInitialLoadComplete.asStateFlow()

    // ---------------------------------------------------------
    // Timeline items (filtered)
    // ---------------------------------------------------------
    val timelineEvents: StateFlow<List<TimelineEvent>> =
        timeline.state
            .map { state ->
                state.elements
                    .filterNotNull()
                    .filter(eventFilter)
            }
            .stateIn(scope, SharingStarted.Eagerly, emptyList())

    val isLoadingBefore: StateFlow<Boolean> =
        timeline.state
            .map { it.isLoadingBefore }
            .stateIn(scope, SharingStarted.Eagerly, false)

    val isLoadingAfter: StateFlow<Boolean> =
        timeline.state
            .map { it.isLoadingAfter }
            .stateIn(scope, SharingStarted.Eagerly, false)

    // ---------------------------------------------------------
    // Room lastEventId (truth source)
    // ---------------------------------------------------------
    private val lastEventIdFlow: Flow<String> =
        client.room
            .getById(roomId)
            .mapNotNull { it?.lastEventId?.full }
            .distinctUntilChanged()

    // ---------------------------------------------------------
    // Start coordination
    // ---------------------------------------------------------
    fun start() {
        if (started) return
        started = true
        logger.d { "🚀 Starting RoomTimelineCoordinator for ${roomId.full}" }
        
        scope.launch {
            // 1. Initialize first
            initTimeline()
            
            // 2. Then start observing live updates
            launch { observeNewEvents() }
        }
    }

    // ---------------------------------------------------------
    // Initial timeline load
    // ---------------------------------------------------------
    private suspend fun initTimeline() {
        if (initialized) return
        initialized = true

        val room = client.room.getById(roomId).filterNotNull().first()

        if (!room.membersLoaded) {
            client.user.loadMembers(roomId, wait = false)
        }

        val lastEventId = room.lastEventId
        if (lastEventId == null) {
            logger.w { "⚠ No lastEventId for room ${roomId.full}" }
            _isInitialLoadComplete.value = true
            return
        }

        timeline.init(roomId, lastEventId) {
            maxSize = 30
        }

        // Wait until first page arrives
        timeline.state
            .map { it.elements.isNotEmpty() }
            .first { it }

        // ✅ Backfill if the window is full of call signaling noise
        ensureInitialWindowHasChatMessages()

        _isInitialLoadComplete.value = true
        logger.d { "✅ Timeline initialized for ${roomId.full}" }
    }

    /**
     * Prevents empty screen after calls by ensuring we have at least 
     * some displayable messages in the initial window.
     */
    private suspend fun ensureInitialWindowHasChatMessages() {

        fun isDisplayable(e: TimelineEvent): Boolean {
            val content = e.content?.getOrNull()
            return content is RoomMessageEventContent || content is MemberEventContent
        }

        repeat(5) {
            // ✅ read current state from the flow
            val state = timeline.state.first()
            val elements = state.elements.filterNotNull()

            val displayableCount = elements.count(::isDisplayable)
            if (displayableCount >= 8) return

            logger.d {
                "⚠️ Initial window only has $displayableCount displayable items. Backfilling..."
            }

            val beforeSize = elements.size

            timeline.loadBefore { maxSize = 30 }

            // ✅ wait until timeline actually changes
            timeline.state
                .map { it.elements.size }
                .first { it != beforeSize }
        }
    }


    // ---------------------------------------------------------
    // Observe new messages
    // ---------------------------------------------------------
    private suspend fun observeNewEvents() {
        lastEventIdFlow
            .drop(1)
            .conflate() // Compress bursts (like ICE candidates spam)
            .collectLatest {
                // Larger maxSize ensures we escape the "call signaling bubble" 
                // if many candidates arrive at once
                timeline.loadAfter { maxSize = 50 }
            }
    }

    // ---------------------------------------------------------
    // Pagination API
    // ---------------------------------------------------------
    suspend fun loadBefore() {
        timeline.loadBefore { maxSize = 20 }
    }

    suspend fun loadAfter() {
        timeline.loadAfter { maxSize = 10 }
    }

    // ---------------------------------------------------------
    // Cleanup
    // ---------------------------------------------------------
/*    fun stop() {
        scope.cancel()
    }*/
}
