package com.example.getsafenowclient.home.setting

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import io.getsafenow.libraries.architecture.ScreenComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import net.folivo.trixnity.client.MatrixClient

interface SettingComponent : ScreenComponent {
    val userProfile: Flow<SettingUserProfile>
}

data class SettingUserProfile(
    val userId: String,
    val displayName: String,
    val avatarUrl: String?
)

class SettingScreenModel(
    componentContext: ComponentContext,
    private val client: MatrixClient
) : SettingComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate).also { sc ->
        lifecycle.doOnDestroy { sc.cancel() }
    }

    override val userProfile: Flow<SettingUserProfile> = combine(
        client.displayName,
        client.avatarUrl
    ) { name, avatar ->
        SettingUserProfile(
            userId = client.userId.full,
            displayName = name ?: client.userId.localpart,
            avatarUrl = avatar
        )
    }

    @Composable
    override fun Render() {
        // No-op
    }
}
