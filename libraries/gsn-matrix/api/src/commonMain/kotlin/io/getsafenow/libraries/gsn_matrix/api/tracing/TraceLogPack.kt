package io.getsafenow.libraries.gsn_matrix.api.tracing

enum class TraceLogPack(val key: String) {
    EVENT_CACHE("event_cache") {
        override val title: String = "Event Cache"
    },
    SEND_QUEUE("send_queue") {
        override val title: String = "Send Queue"
    },
    TIMELINE("timeline") {
        override val title: String = "Timeline"
    },
    NOTIFICATION_CLIENT("notification_client") {
        override val title: String = "Notification Client"
    };

    abstract val title: String
}
