package io.getsafenow.features.api

interface LoginIntentResolver {
    fun parse(uriString: String): LoginParams?
}
