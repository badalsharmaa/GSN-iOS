package com.example.getsafenowclient.utils

import kotlin.jvm.JvmStatic


object CallUtils {

    @JvmStatic
    fun isVideoOffer(sdp: String): Boolean {
        val videoLine = sdp
            .lineSequence()
            .firstOrNull { it.startsWith("m=video ") }
            ?: return false

        return !videoLine.startsWith("m=video 0")
    }
}