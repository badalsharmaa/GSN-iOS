package com.example.getsafenowclient.component.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.getsafenowclient.common.ui.GsnAvatarAdvanced
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Regular
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.regular.TimesCircle
import compose.icons.fontawesomeicons.solid.ChevronRight
import compose.icons.fontawesomeicons.solid.Pen
import io.getsafenow.libraries.designcomponents.utils.preview.GsnPreview
import io.getsafenow.libraries.gsn_theme.customtheme.GsnTheme
import net.folivo.trixnity.client.MatrixClient
import org.jetbrains.compose.ui.tooling.preview.Preview

// ---------------------------------------------------------
// 1. Header Component
// ---------------------------------------------------------
@Composable
fun SettingsHeader(
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Settings",
            style = GsnTheme.typography.fontHeadingLgBold,
            color = GsnTheme.colors.textPrimary
        )
        IconButton(
            onClick = onCloseClick,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = FontAwesomeIcons.Regular.TimesCircle,
                contentDescription = "Close",
                tint = GsnTheme.colors.iconSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ---------------------------------------------------------
// 2. Profile Image Section
// ---------------------------------------------------------
@Composable
fun SettingsProfileSection(
    userId: String,
    userName: String,
    avatarUrl: String?,
    client: MatrixClient?,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box {
            GsnAvatarAdvanced(
                modifier = Modifier.size(100.dp),
                id = userId,
                name = userName,
                url = avatarUrl,
                client = client,
                textSize = 32.sp
            )

            // Edit Button (Pencil)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp) // Offset slightly
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(GsnTheme.colors.iconPrimary) // Dark background as per image
                    .clickable(onClick = onEditClick)
                    .border(2.dp, GsnTheme.colors.bgCanvasDefault, CircleShape), // Optional white border for contrast
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = FontAwesomeIcons.Solid.Pen,
                    contentDescription = "Edit Profile",
                    tint = GsnTheme.colors.iconOnSolidPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

// ---------------------------------------------------------
// 3. Arrow Button (Menu Item)
// ---------------------------------------------------------
@Composable
fun SettingsMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = text,
                style = GsnTheme.typography.fontBodyLgMedium,
                color = GsnTheme.colors.textPrimary
            )
            Icon(
                imageVector = FontAwesomeIcons.Solid.ChevronRight,
                contentDescription = null,
                tint = GsnTheme.colors.iconSecondary,
                modifier = Modifier.size(14.dp)
            )
        }
        if (showDivider) {
            HorizontalDivider(
                color = GsnTheme.colors.borderInteractiveSecondary, // Consistent with HomeComponents
                thickness = 1.dp
            )
        }
    }
}

// ---------------------------------------------------------
// 4. Assembled Screen Content (Example Usage)
// ---------------------------------------------------------
@Composable
fun SettingsScreenContent(
    userId: String,
    userName: String,
    avatarUrl: String?,
    client: MatrixClient?,
    onClose: () -> Unit,
    onEditProfile: () -> Unit,
    onMenuItemClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(GsnTheme.colors.bgCanvasDefault)
    ) {
        // 1. Header
        SettingsHeader(onCloseClick = onClose)

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Profile Section
        SettingsProfileSection(
            userId = userId,
            userName = userName,
            avatarUrl = avatarUrl,
            client = client,
            onEditClick = onEditProfile
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 3. Menu Items
        // Top divider
        HorizontalDivider(color = GsnTheme.colors.borderInteractiveSecondary, thickness = 1.dp) 
        SettingsMenuItem(text = "Profile", onClick = { onMenuItemClick("Profile") })
        SettingsMenuItem(text = "Contacts", onClick = { onMenuItemClick("Contacts") })
        SettingsMenuItem(text = "Notifications", onClick = { onMenuItemClick("Notifications") })
        SettingsMenuItem(text = "Secure Account Backup", onClick = { onMenuItemClick("Backup") })
    }
}

// ---------------------------------------------------------
// PREVIEW
// ---------------------------------------------------------
@Preview
@Composable
private fun SettingsScreenPreview() {
    GsnPreview {
        SettingsScreenContent(
            userId = "@bhavuk:example.com",
            userName = "Bhavuk",
            avatarUrl = null,
            client = null,
            onClose = {},
            onEditProfile = {},
            onMenuItemClick = {}
        )
    }
}
