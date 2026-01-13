package com.example.getsafenowclient.home.setting

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

@Composable
fun settingPresenter(
    settingComponent: SettingComponent,
    onClose: () -> Unit,
    onOpenProfile: () -> Unit,
): Pair<SettingState, (SettingEvent) -> Unit> {

    val userProfile by settingComponent.userProfile.collectAsState(initial = null)

    val state = SettingState(
        userId = userProfile?.userId ?: "",
        userName = userProfile?.displayName ?: "",
        userAvatarUrl = userProfile?.avatarUrl,
        isLoading = userProfile == null
    )

    val eventSink: (SettingEvent) -> Unit = remember {
        { event ->
            when (event) {
                SettingEvent.Close -> onClose()
                SettingEvent.EditProfile -> onOpenProfile()
                is SettingEvent.MenuItemClicked -> {
                    if (event.item == "Profile") {
                        onOpenProfile()
                    }
                    // Handle other menu items if needed
                }
            }
        }
    }

    return state to eventSink
}
