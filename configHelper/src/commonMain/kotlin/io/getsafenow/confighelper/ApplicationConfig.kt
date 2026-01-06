package io.getsafenow.confighelper

object ApplicationConfig {
    /**
     * Application name used in the UI for string. If empty, the value is taken from the resources `R.string.app_name`.
     * Note that this value is not used for the launcher icon.
     * For GetSafeNow, the value is empty, and so read from `R.string.app_name`, which depends on the build variant:
     * - "GetSafeNow Client" for release builds;
     * - "GetSafeNow Client dbg" for debug builds;
     * - "GetSafeNow Client nightly" for nightly builds.
     */
    const val APPLICATION_NAME: String = ""

    /**
     * Used in the strings to reference the GetSafeNow client.
     * Cannot be empty.
     * For GetSafeNow, the value is "GetSafeNow".
     */
    const val PRODUCTION_APPLICATION_NAME: String = "GetSafeNow"

    /**
     * Used in the strings to reference the GetSafeNow Desktop client, for instance GetSafeNow Web.
     * Cannot be empty.
     * For GetSafeNow, the value is "GetSafeNow". We use the same name for desktop and mobile for now.
     */
    const val DESKTOP_APPLICATION_NAME: String = "GetSafeNow"
}
