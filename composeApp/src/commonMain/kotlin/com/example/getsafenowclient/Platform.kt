package com.example.getsafenowclient

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform