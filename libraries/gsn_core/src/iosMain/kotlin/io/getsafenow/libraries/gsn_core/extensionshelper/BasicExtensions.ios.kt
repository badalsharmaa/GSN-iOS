package io.getsafenow.libraries.gsn_core.extensionshelper

import kotlinx.cinterop.BetaInteropApi
import platform.Foundation.NSString
import platform.Foundation.create
import platform.Foundation.decomposedStringWithCanonicalMapping

@OptIn(BetaInteropApi::class)
actual fun String.withoutAccents(): String {
    val ns = NSString.create(string = this)
    return ns.decomposedStringWithCanonicalMapping
        .replace(Regex("\\p{Mn}+"), "")
}

actual fun platformCapitalize(input: String): String {
    return input.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase() else it.toString()
    }
}
