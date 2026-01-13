package com.example.getsafenowclient.room.sharing



sealed interface VoiceMessageEvent {
    object StartRecording : VoiceMessageEvent
    object StopRecording : VoiceMessageEvent
    object CancelRecording : VoiceMessageEvent
    object SendRecording : VoiceMessageEvent
    object DismissDialog : VoiceMessageEvent
}
