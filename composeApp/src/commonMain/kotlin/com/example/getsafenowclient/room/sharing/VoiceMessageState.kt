package com.example.getsafenowclient.room.sharing

enum class VoiceRecorderState {
    Idle,
    Recording,
    Review
}

data class VoiceRecorderUiState(
    val state: VoiceRecorderState = VoiceRecorderState.Idle,
    val durationSeconds: Int = 0,
    val maxDurationSeconds: Int = 60,
    val progress: Float = 0f
) {
    val remainingSeconds: Int get() = max(0, maxDurationSeconds - durationSeconds)
}

fun max(a: Int, b: Int): Int = if (a > b) a else b