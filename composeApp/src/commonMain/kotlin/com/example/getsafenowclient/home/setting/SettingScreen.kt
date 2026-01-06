package com.example.getsafenowclient.home.setting

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.getsafenowclient.component.GsnLoader
import com.example.getsafenowclient.component.setting.SettingsScreenContent
import com.example.getsafenowclient.home.setting.avatar.AvatarChangeEvent
import com.example.getsafenowclient.home.setting.avatar.avatarChangePresenter
import com.example.getsafenowclient.photopicker.rememberPhotoPickerLauncher
import io.getsafenow.libraries.gsn_theme.customtheme.GsnTheme
import net.folivo.trixnity.client.MatrixClient

@Composable
fun SettingScreen(
    settingComponent: SettingComponent,
    client: MatrixClient,
    onClose: () -> Unit,
    onOpenProfile: () -> Unit
) {
    val (state, eventSink) = settingPresenter(
        settingComponent = settingComponent,
        onClose = onClose,
        onOpenProfile = onOpenProfile
    )

    // Avatar Change Logic
    val (avatarState, avatarEventSink) = avatarChangePresenter(client)

    val photoPickerLauncher = rememberPhotoPickerLauncher { bytes ->
        if (bytes != null) {
            avatarEventSink(AvatarChangeEvent.AvatarSelected(bytes))
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = GsnTheme.colors.bgCanvasDefault
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Use key to force recomposition of the content when the avatar URL changes.
            // This helps ensure that the GsnAvatarAdvanced component refreshes the image.
            key(state.userAvatarUrl) {
                SettingsScreenContent(
                    userId = state.userId,
                    userName = state.userName,
                    avatarUrl = state.userAvatarUrl,
                    client = client,
                    onClose = { eventSink(SettingEvent.Close) },
                    onEditProfile = {
                        // Launch Photo Picker to change avatar
                        photoPickerLauncher.launch()
                    },
                    onMenuItemClick = { item -> eventSink(SettingEvent.MenuItemClicked(item)) }
                )
            }

            if (avatarState.isUploading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    GsnLoader(modifier = Modifier.size(45.dp))
                }
            }
        }
    }
}
