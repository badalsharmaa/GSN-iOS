package io.getsafenow.confighelper

import io.getsafenow.appconfig.BuildKonfig

object RageshakeConfig {
    /**
     * The URL to submit bug reports to.
     */
     val BUG_REPORT_URL = BuildKonfig.BUG_REPORT_URL

    /**
     * As per https://github.com/matrix-org/rageshake:
     * Identifier for the application (eg 'riot-web').
     * Should correspond to a mapping configured in the configuration file for github issue reporting to work.
     */
     val BUG_REPORT_APP_NAME = BuildKonfig.BUG_REPORT_APP_NAME

    /**
     * The maximum size of the upload request. Default value is just below CloudFlare's max request size.
     */
    const val MAX_LOG_UPLOAD_SIZE = 50 * 1024 * 1024L
}
