package io.getsafenow.libraries.kmputils.clipboard

class FakeClipboardHelper : ClipboardHelper {
    var clipboardContents: Any? = null

    override fun copyPlainText(text: String) {
        clipboardContents = text
    }
}
