package io.getsafenow.libraries.featureflag.api

import io.getsafenow.libraries.gsn_core.meta.BuildMeta

interface Feature {
    /**
     * Unique key to identify the feature.
     */
    val key: String

    /**
     * Title to show in the UI. Not needed to be translated as it's only dev accessible.
     */
    val title: String

    /**
     * Optional description to give more context on the feature.
     */
    val description: String?

    /**
     * Calculate the default value of the feature (enabled or disabled) given a [BuildMeta].
     */
    val defaultValue: (BuildMeta) -> Boolean

    /**
     * Whether the feature is finished or not.
     * If false: the feature is still in development, it will appear in the developer options screen to be able to enable it and test it.
     * If true: the feature is finished, it will not appear in the developer options screen.
     */
    val isFinished: Boolean
}
