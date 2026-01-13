package io.getsafenow.libraries.gsn_matrix.api.core

import io.getsafenow.libraries.kmputils.metadata.isInDebug
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * A [String] holding a valid Matrix user ID.
 *
 * https://spec.matrix.org/v1.8/appendices/#user-identifiers
 */
@JvmInline
@Serializable
value class UserId(val value: String) {
    init {
        // Only validate in debug builds to avoid performance impact in release
        if (isInDebug && !MPatternsGsn.isUserId(value)) {
            error("`$value` is not a valid user id.\nExample user id: `@name:domain`.")
        }
    }

    override fun toString(): String = value

    val extractedDisplayName: String
        get() = value
            .removePrefix("@")
            .substringBefore(":")

    val domainName: String?
        get() = value.substringAfter(":").takeIf { it.isNotEmpty() }

    companion object {
        /**
         * Creates a UserId from a string, returning null if invalid.
         * Use this for user input validation.
         */
        fun fromStringOrNull(value: String?): UserId? {
            return if (value != null && MPatternsGsn.isUserId(value)) {
                UserId(value)
            } else {
                null
            }
        }

        /**
         * Creates a UserId from a string, throwing an exception if invalid.
         * Use this when you're certain the input is valid.
         */
        fun fromString(value: String): UserId = UserId(value)
    }
}