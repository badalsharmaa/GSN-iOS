package io.getsafenow.libraries.gsn_matrix.api.timeline.item.event

enum class MembershipChange {
    NONE,
    ERROR,
    JOINED,
    LEFT,
    BANNED,
    UNBANNED,
    KICKED,
    INVITED,
    KICKED_AND_BANNED,
    INVITATION_ACCEPTED,
    INVITATION_REJECTED,
    INVITATION_REVOKED,
    KNOCKED,
    KNOCK_ACCEPTED,
    KNOCK_RETRACTED,
    KNOCK_DENIED,
    NOT_IMPLEMENTED
}
