package io.getsafenow.libraries.gsn_core.log.logger

/**
 * Parent class for custom logger tags. Can be used with Timber :
 *
 * val loggerTag = LoggerTag("MyTag", LoggerTag.VOIP)
 * Timber.tag(loggerTag.value).v("My log message")
 */
open class LoggerTag(name: String, parentTag: LoggerTag? = null) {
    object PushLoggerTag : LoggerTag("Push")
    object NotificationLoggerTag : LoggerTag("Notification", PushLoggerTag)

    val value: String = if (parentTag == null) {
        name
    } else {
        "${parentTag.value}/$name"
    }
}
