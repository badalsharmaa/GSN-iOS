package io.getsafenow.libraries.kmputils.clipboard

import io.getsafenow.libraries.di.ApplicationContextGsn
import me.tatarka.inject.annotations.Inject


@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class AndroidClipboardHelper @Inject constructor(
    @ApplicationContextGsn private val context: Context,
) : ClipboardHelper {
    private val clipboardManager = requireNotNull(context.getSystemService<ClipboardManager>())

    override fun copyPlainText(text: String) {
        clipboardManager.setPrimaryClip(ClipData.newPlainText("", text))
    }
}
