package io.getsafenow.services.toolkit.api.intent

import android.content.Intent


actual class NativeIntent(val intent: Intent) {
    // Safe for host unit tests (no Uri.parse, no putExtra, no flags).
    actual constructor() : this(Intent())
}