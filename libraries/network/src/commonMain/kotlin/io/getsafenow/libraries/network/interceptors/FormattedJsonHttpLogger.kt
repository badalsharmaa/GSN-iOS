package io.getsafenow.libraries.network.interceptors

import co.touchlab.kermit.Logger
import io.getsafenow.libraries.gsn_core.extensionshelper.ellipsize
import io.ktor.client.plugins.logging.LogLevel
import kotlinx.coroutines.launch
import io.ktor.client.plugins.logging.Logger as KtorLogger
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject

/**
 * KMP-compatible HTTP logger that formats JSON responses for better debugging.
 *
 * This logger does exactly what android FormattedJsonHttpLogger does:
 * - Logs message with ellipsize truncation
 * - Formats JSON objects and arrays with proper indentation
 * - Uses KMP-compatible synchronization
 * - Non-blocking coroutine execution for iOS compatibility
 */
internal class FormattedJsonHttpLogger(
    private val level: LogLevel,
    private val logger: Logger = Logger
) : KtorLogger {

    companion object {
        private const val INDENT_SPACE = 2
    }

    private val logMutex = Mutex()
    private val json = Json {
        prettyPrint = true
        prettyPrintIndent = " ".repeat(INDENT_SPACE)
    }

    override fun log(message: String) {
        // Non-blocking coroutine scope - better for iOS compatibility
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default).launch {
            logMutex.withLock {
                logMessageInternal(message)
            }
        }
    }

    private suspend fun logMessageInternal(message: String) {
        // Always log the message first with ellipsize truncation
        logger.v { message.ellipsize(200_000) }

        // Only format JSON if level is BODY
        if (level != LogLevel.BODY) return

        // Only format JSON if message length <= 100,000
        if (message.length > 100_000) {
            logger.d { "Content is too long (${message.length} chars) to be formatted as JSON" }
            return
        }

        // Simplified JSON parsing - handles both objects and arrays
        if (message.startsWith("{") || message.startsWith("[")) {
            try {
                val jsonElement = json.parseToJsonElement(message)
                if (jsonElement is JsonObject || jsonElement is JsonArray) {
                    logJson(jsonElement.toString()) // prettyPrint applies automatically
                }
            } catch (e: Exception) {
                logger.e("Failed to parse JSON", e)
            }
        }
    }

    private fun logJson(formattedJson: String) {
        // Remove empty lines at the end and log each line
        formattedJson
            .trimEnd()
            .lines()
            .forEach { line ->
                logger.v { line }
            }
    }
}