package io.getsafenow.services.toolkit.api.intent


actual class NativeIntent(val url: String) {
    // Safe dummy for tests
    actual constructor() : this("")
}

