package com.example.getsafenowclient.home


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.getsafenowclient.common.ui.GsnAvatarAdvanced
import com.example.getsafenowclient.utils.toText
import io.getsafenow.libraries.gsn_theme.customtheme.GsnTheme
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.core.model.RoomId
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class RoomHeader @OptIn(ExperimentalTime::class) constructor(
    val id: RoomId,
    val title: String,
    val lastMessageText: String,
    val lastMessageDate: Instant,
    val unreadCount: Long,
    val avatarUrl: String?,
    val isSpace: Boolean
) {
    @OptIn(ExperimentalTime::class)
    @Composable
    fun render(modifier: Modifier = Modifier, client: MatrixClient) {
        if (!isSpace) {
            Row(
                modifier = modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GsnAvatarAdvanced(
                    modifier = Modifier.padding(8.dp).size(40.dp),
                    client = client,
                    id = id.full,
                    name = title,
                    url = avatarUrl
                )
                Column(
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            // ✅ Use GSN Typography and Color
                            style = GsnTheme.typography.fontBodyMdMedium,
                            color = GsnTheme.colors.textPrimary
                        )
                        // Convert kotlinx.datetime.Instant → kotlin.time.Instant for your toText()
                        val timeText = Instant
                            .fromEpochMilliseconds(lastMessageDate.toEpochMilliseconds())
                            .toText()
                        Text(
                            // ✅ Use GSN Typography and Color
                            style = GsnTheme.typography.fontBodySmRegular,
                            color = GsnTheme.colors.textSecondary,
                            text = timeText
                        )
                    }
                    Spacer(modifier = Modifier.size(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = lastMessageText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            // ✅ Use GSN Typography and Color
                            style = GsnTheme.typography.fontBodySmRegular,
                            color = GsnTheme.colors.textSecondary
                        )
                        if (unreadCount > 0) {
                            // ✅ Style the badge with GSN Colors
                            Badge(
                                containerColor = GsnTheme.colors.bgBadgeAccent,
                                contentColor = GsnTheme.colors.textBadgeAccent
                            ) {
                                Text(
                                    text = if (unreadCount < 100) unreadCount.toString() else "99+",
                                    style = GsnTheme.typography.fontBodyXsMedium
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.tertiaryContainer),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GsnAvatarAdvanced(
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp).size(24.dp),
                    client = client,
                    id = id.full,
                    name = title,
                    url = avatarUrl,
                    shape = RoundedCornerShape(20)
                )
                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    // ✅ Use GSN Typography and Color
                    style = GsnTheme.typography.fontBodyMdMedium,
                    color = GsnTheme.colors.textPrimary
                )
            }
        }
    }
}