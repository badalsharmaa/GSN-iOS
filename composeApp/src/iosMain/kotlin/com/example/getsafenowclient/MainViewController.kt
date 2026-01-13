package com.example.getsafenowclient

import androidx.compose.ui.window.ComposeUIViewController
import io.getsafenow.libraries.kmputils.platformkmp.ContextFactory

fun MainViewController() = ComposeUIViewController { App(contextFactory = ContextFactory()) }