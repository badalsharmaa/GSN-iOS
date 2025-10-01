package io.getsafenow.services.toolkit.api.tests

import io.getsafenow.services.toolkit.api.intent.NativeIntent

import android.content.Intent
import android.net.Uri


actual fun nativeIntentFromUrl(url: String): NativeIntent {
    // Do NOT call Uri.parse() in JVM unit tests
    val intent = Intent(Intent.ACTION_VIEW).apply {
        putExtra("TEST_URL", url) // keep the URL around if you ever want to assert on it
    }
    return NativeIntent(intent)
}