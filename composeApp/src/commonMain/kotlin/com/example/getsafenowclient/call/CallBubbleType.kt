package com.example.getsafenowclient.call

enum class CallBubbleType {
    OUTGOING_RINGING,
    OUTGOING_MISSED,
    OUTGOING_CANCELLED,
    OUTGOING_ENDED,

    INCOMING_RINGING,
    INCOMING_MISSED,
    INCOMING_DECLINED,
    INCOMING_ENDED,

    FAILED
}
