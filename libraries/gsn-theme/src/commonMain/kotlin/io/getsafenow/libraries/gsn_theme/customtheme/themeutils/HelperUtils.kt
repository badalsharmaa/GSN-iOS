package io.getsafenow.libraries.gsn_theme.customtheme.themeutils

@Suppress("ExperimentalAnnotationRetention")
@RequiresOptIn(
    message = "⚠️ This is a GetSafeNow Core color token. " +
            "Do NOT use it directly in UI components. " +
            "Always map it to a semantic color (e.g., gsn_BgPrimary, gsn_TextError). " +
            "If you use it raw, it will NOT adapt correctly to light/dark mode."
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.CLASS)
annotation class GsnCoreColorToken
