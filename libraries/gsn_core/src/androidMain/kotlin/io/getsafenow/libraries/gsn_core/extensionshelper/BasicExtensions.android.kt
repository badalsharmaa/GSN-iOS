package io.getsafenow.libraries.gsn_core.extensionshelper

import java.text.Normalizer
import java.util.Locale

actual fun String.withoutAccents(): String {
    return Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace("\\p{Mn}+".toRegex(), "")
}

actual fun platformCapitalize(input: String): String {
    return input.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    }
}