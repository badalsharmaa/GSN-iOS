package io.getsafenow.libraries.gsn_core.extensionshelper

// Boolean extensions
fun Boolean.toOnOff() = if (this) "ON" else "OFF"
fun Boolean.to01() = if (this) "1" else "0"

// Utility "also" shortcut
inline fun <T> T.ooi(block: (T) -> Unit): T = also(block)

/**
 * Return empty CharSequence if the CharSequence is null.
 */
fun CharSequence?.orEmpty() = this ?: ""

/**
 * Append [insert] before the last [delimiter] in the string.
 */
fun String?.insertBeforeLast(insert: String, delimiter: String = "."): String {
    if (this == null) return insert
    val idx = lastIndexOf(delimiter)
    return if (idx == -1) this + insert else replaceRange(idx, idx, insert)
}

/**
 * Truncate and ellipsize text if it exceeds the given length.
 *
 * Throws if [length] < 1.
 */
fun String.ellipsize(length: Int): String {
    require(length >= 1)
    return if (this.length <= length) this else "${this.take(length)}…"
}

/**
 * Replace the old prefix with the new prefix.
 */
fun String.replacePrefix(oldPrefix: String, newPrefix: String): String {
    return if (startsWith(oldPrefix)) newPrefix + substring(oldPrefix.length) else this
}

/**
 * Surround with brackets.
 */
fun String.withBrackets(prefix: String = "(", suffix: String = ")") = "$prefix$this$suffix"

/**
 * Capitalize the string.
 * Delegates to platform-specific implementation.
 */
fun String.safeCapitalize(): String = platformCapitalize(this)

/**
 * Remove accents/diacritics from the string.
 * Delegates to platform-specific implementation.
 */
expect fun String.withoutAccents(): String

/**
 * Expect/actual for locale-aware capitalization.
 */
expect fun platformCapitalize(input: String): String

// RTL / LTR handling
private const val RTL_OVERRIDE_CHAR = '\u202E'
private const val LTR_OVERRIDE_CHAR = '\u202D'

fun String.ensureEndsLeftToRight() = if (containsRtLOverride()) "$this$LTR_OVERRIDE_CHAR" else this
fun String.containsRtLOverride() = contains(RTL_OVERRIDE_CHAR)
fun String.filterDirectionOverrides() = filterNot { it == RTL_OVERRIDE_CHAR || it == LTR_OVERRIDE_CHAR }

/**
 * Safe truncation with optional ellipsis.
 */
fun String.toSafeLength(maxLength: Int = 500, ellipsize: Boolean = false): String {
    return if (ellipsize) ellipsize(maxLength)
    else if (length > maxLength) take(maxLength)
    else this
}

