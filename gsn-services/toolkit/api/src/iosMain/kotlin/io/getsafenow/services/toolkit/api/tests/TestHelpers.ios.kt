package io.getsafenow.services.toolkit.api.tests

import io.getsafenow.services.toolkit.api.intent.NativeIntent


actual fun nativeIntentFromUrl(url: String): NativeIntent = NativeIntent(url)