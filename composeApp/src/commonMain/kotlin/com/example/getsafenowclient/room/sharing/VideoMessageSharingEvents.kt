package com.example.getsafenowclient.room.sharing

sealed interface VideoMessageEvent {
    data object StartRecording : VideoMessageEvent
    data object StopRecording : VideoMessageEvent
    data object CancelRecording : VideoMessageEvent
    data object SendRecording : VideoMessageEvent
    data object SwitchCamera : VideoMessageEvent
    data object DismissDialog : VideoMessageEvent
}
