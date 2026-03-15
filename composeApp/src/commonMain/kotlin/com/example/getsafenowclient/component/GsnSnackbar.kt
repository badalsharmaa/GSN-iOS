package com.example.getsafenowclient.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Bell
import compose.icons.fontawesomeicons.solid.Times
import compose.icons.fontawesomeicons.solid.User
import io.getsafenow.libraries.gsn_theme.customtheme.GsnTheme

/**
 * Custom Snackbar matching the GSN Design System (similar to CallOverlayBanner).
 */
@Composable
fun GsnSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier
    ) { data ->
        GsnSnackbar(data)
    }
}

@Composable
fun GsnSnackbar(
    data: SnackbarData
) {
    // Determine style based on visuals
    val backgroundColor = GsnTheme.colors.bgSubtlePrimary // Level 2 Elevation Surface
    val contentColor = GsnTheme.colors.textPrimary
    
    // Parse standard format "Sender: Body" if present to style differently
    val text = data.visuals.message
    val (title, body) = if (text.contains(": ")) {
        text.split(": ", limit = 2).let { it[0] to it[1] }
    } else {
        "New Message" to text
    }

    Surface(
        modifier = Modifier
            .padding(8.dp) // Slightly tighter padding for banner feel
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        shadowElevation = 8.dp // High elevation for floating banner
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon: Use User icon to feel "social"
            Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = GsnTheme.colors.bgAccentRest,
                modifier = Modifier.size(40.dp)
            ) {
                 androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
                     Icon(
                        imageVector = FontAwesomeIcons.Solid.User,
                        contentDescription = null,
                        tint = GsnTheme.colors.iconOnSolidPrimary,
                        modifier = Modifier.size(20.dp)
                     )
                 }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Title (Sender Name) - Medium (Bold not available)
                Text(
                    text = title,
                    style = GsnTheme.typography.fontBodyMdMedium, 
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                // Body (Message) - Regular
                Text(
                    text = body,
                    style = GsnTheme.typography.fontBodySmRegular, // Smaller for body
                    color = contentColor.copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Action (Dismiss)
            IconButton(
                onClick = { data.dismiss() },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = FontAwesomeIcons.Solid.Times,
                    contentDescription = "Dismiss",
                    tint = GsnTheme.colors.iconSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
