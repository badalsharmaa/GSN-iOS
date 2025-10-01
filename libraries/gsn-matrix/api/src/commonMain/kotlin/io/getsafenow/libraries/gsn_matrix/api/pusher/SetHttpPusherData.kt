package io.getsafenow.libraries.gsn_matrix.api.pusher

data class SetHttpPusherData(
    val pushKey: String,
    val appId: String,
    val url: String,
    val appDisplayName: String,
    val deviceDisplayName: String,
    val profileTag: String?,
    val lang: String,
    val defaultPayload: String,
)
