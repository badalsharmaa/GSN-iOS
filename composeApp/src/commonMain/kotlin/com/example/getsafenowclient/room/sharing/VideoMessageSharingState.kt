package com.example.getsafenowclient.room.sharing

enum class VideoRecorderState {
    Idle,
    Recording,
    Loading, // Processing/Finalizing video
    Review
}

data class VideoRecorderUiState(
    val state: VideoRecorderState = VideoRecorderState.Idle,
    val durationSeconds: Int = 0,
    val maxDurationSeconds: Int = 60,
    val progress: Float = 0f
) {
    val remainingSeconds: Int get() = if (maxDurationSeconds > durationSeconds) maxDurationSeconds - durationSeconds else 0
}
