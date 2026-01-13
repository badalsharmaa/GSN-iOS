package com.example.getsafenowclient.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.example.getsafenowclient.call.CallScreenModel
import com.example.getsafenowclient.home.invite.InviteRoute
import com.example.getsafenowclient.home.setting.SettingRoot
import com.example.getsafenowclient.home.setting.profile.ProfileRoot
import com.example.getsafenowclient.room.RoomComponentImpl
import com.example.getsafenowclient.room.RoomScreen
import com.example.getsafenowclient.room.di.RoomComponentStore
import com.example.getsafenowclient.service.SessionManager
import io.getsafenow.libraries.kmputils.platformkmp.ContextFactory
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.core.model.RoomId

@Composable
fun HomeRoot(
    sessionManager: SessionManager,
    roomComponentStore: RoomComponentStore,
    client: MatrixClient,
    contextFactory: ContextFactory,
    callModel: CallScreenModel, // 📞 Global model passed from App.kt
    isPhoneMode: Boolean,
    onRequireLogin: () -> Unit
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val compact = maxWidth < 600.dp

        val componentContext = remember { DefaultComponentContext(LifecycleRegistry()) }
        val homeComponent = remember { HomeViewScreenModel(componentContext, client) }

        var selectedRoomId by remember { mutableStateOf<RoomId?>(null) }
        var showInvites by remember { mutableStateOf(false) }
        var showSettings by remember { mutableStateOf(false) }
        var showProfile by remember { mutableStateOf(false) }

        fun closeOverlays() {
            showInvites = false
            showSettings = false
            showProfile = false
        }

        if (compact) {
            if (showProfile) {
                ProfileRoot(
                    componentContext = componentContext,
                    client = client,
                    sessionManager = sessionManager,
                    onBack = { showProfile = false },
                    onClose = { closeOverlays() },
                    onSignOutComplete = onRequireLogin
                )
            } else if (showSettings) {
                SettingRoot(
                    componentContext = componentContext,
                    client = client,
                    onClose = { showSettings = false },
                    onOpenProfile = { showProfile = true }
                )
            } else if (showInvites) {
                InviteRoute(
                    homeViewScreenModel = homeComponent,
                    client = client,
                    onOpenRoom = { rid ->
                        showInvites = false
                        selectedRoomId = rid
                    },
                    onClose = { showInvites = false }
                )
            } else if (selectedRoomId == null) {
                HomeNewScreen(
                    homeViewScreenModel = homeComponent,
                    client = client,
                    onOpenRoom = { rid -> selectedRoomId = rid },
                    onOpenInvites = { showInvites = true },
                    onOpenSettings = { showSettings = true },
                    sessionManager = sessionManager,
                    onRequireLogin = onRequireLogin
                )
            } else {
                val roomComponent =
                    roomComponentStore.get(selectedRoomId!!) {
                        RoomComponentImpl(
                            componentContext = componentContext,
                            roomId = selectedRoomId!!,
                            client = client,
                            contextFactory = contextFactory,
                            sessionManager = sessionManager,
                            callModel = callModel // 📞 Inject global model
                        )
                    }
                RoomScreen(
                    component = roomComponent,
                    client = client,
                    roomId = selectedRoomId!!,
                    onBack = { selectedRoomId = null }
                )
            }
        } else {
            if (showProfile) {
                ProfileRoot(
                    componentContext = componentContext,
                    client = client,
                    sessionManager = sessionManager,
                    onBack = { showProfile = false },
                    onClose = { closeOverlays() },
                    onSignOutComplete = onRequireLogin
                )
            } else if (showSettings) {
                SettingRoot(
                    componentContext = componentContext,
                    client = client,
                    onClose = { showSettings = false },
                    onOpenProfile = { showProfile = true }
                )
            } else {
                Row(Modifier.fillMaxSize()) {
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .width(350.dp)
                    ) {
                        HomeNewScreen(
                            homeViewScreenModel = homeComponent,
                            client = client,
                            onOpenRoom = { rid -> selectedRoomId = rid },
                            onOpenInvites = { showInvites = true },
                            onOpenSettings = { showSettings = true },
                            sessionManager = sessionManager,
                            onRequireLogin = onRequireLogin
                        )
                    }
                    HorizontalDivider(
                        Modifier
                            .fillMaxHeight()
                            .width(DividerDefaults.Thickness)
                    )
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .weight(1f)
                    ) {
                        if (showInvites) {
                            InviteRoute(
                                homeViewScreenModel = homeComponent,
                                client = client,
                                onOpenRoom = { rid ->
                                    showInvites = false
                                    selectedRoomId = rid
                                },
                                onClose = { showInvites = false }
                            )
                        } else {
                            selectedRoomId?.let { rid ->
                                val roomComponent =
                                    roomComponentStore.get(selectedRoomId!!) {
                                        RoomComponentImpl(
                                            componentContext = componentContext,
                                            roomId = selectedRoomId!!,
                                            client = client,
                                            contextFactory = contextFactory,
                                            sessionManager = sessionManager,
                                            callModel = callModel // 📞 Inject global model
                                        )
                                    }
                                RoomScreen(
                                    component = roomComponent,
                                    client = client,
                                    roomId = rid,
                                    onBack = null
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
