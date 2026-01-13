package io.getsafenow.appconfig

/**
 * Factory for creating AppConfig instances.
 * This provides a single point of access for configuration.
 */
object AppConfigFactory {
    /**
     * Creates a new AppConfig instance.
     * @return AppConfig implementation
     */
    fun create(): AppConfig = AppConfigImpl()

    /**
     * Gets the default AppConfig instance.
     * @return Default AppConfig implementation
     */
    val default: AppConfig by lazy { create() }
}