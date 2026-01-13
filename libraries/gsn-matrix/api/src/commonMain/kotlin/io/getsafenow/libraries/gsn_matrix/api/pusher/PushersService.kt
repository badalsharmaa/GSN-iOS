package io.getsafenow.libraries.gsn_matrix.api.pusher

interface PushersService {
    suspend fun setHttpPusher(setHttpPusherData: SetHttpPusherData): Result<Unit>
    suspend fun unsetHttpPusher(unsetHttpPusherData: UnsetHttpPusherData): Result<Unit>
}
