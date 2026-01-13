package io.getsafenow.libraries.gsn_matrix.api.analytics

/**
 * Analytics trigger for room join events.
 * Tracks how users join rooms for analytics purposes.
 */
enum class GsnJoinRoomTrigger {
    Invite,

    MobilePermalink,

    Notification,

    RoomDirectory,

    RoomPreview,

    SlashCommand,

    SpaceHierarchy,

    Timeline;
}