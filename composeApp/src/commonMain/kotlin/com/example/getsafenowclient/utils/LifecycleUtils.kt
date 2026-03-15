package com.example.getsafenowclient.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.native.concurrent.ThreadLocal

object LifecycleUtils {
    private val _isAppForeground = MutableStateFlow(false)
    val isAppForeground = _isAppForeground.asStateFlow()

    fun onAppForeground() {
        _isAppForeground.value = true
    }

    fun onAppBackground() {
        _isAppForeground.value = false
    }
}
