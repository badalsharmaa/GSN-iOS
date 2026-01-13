package io.getsafenow.libraries.gsn_matrix.api.core

import io.getsafenow.libraries.gsn_matrix.api.permalink.PermalinkData
import io.getsafenow.libraries.gsn_matrix.api.permalink.PermalinkParser

/**
 * Matrix ID/alias pattern utilities for GetSafeNow.
 * Ref: https://spec.matrix.org/latest/appendices/#identifier-grammar
 */
object MPatternsGsn {
    // TLD is not mandatory (localhost, IP address...)
    private const val DOMAIN_REGEX = ":[A-Za-z0-9.-]+(:[0-9]{2,5})?"

    private const val BASE_64_ALPHABET = "[0-9A-Za-z/\\+=]+"
    private const val BASE_64_URL_SAFE_ALPHABET = "[0-9A-Za-z/\\-_]+"

    // User IDs
    private const val M_USER_IDENTIFIER_REGEX = "^@\\S*?$DOMAIN_REGEX$"
    private val PATTERN_CONTAIN_M_USER_IDENTIFIER =
        M_USER_IDENTIFIER_REGEX.toRegex()

    // Room IDs (pre/post MSC4291)
    private const val M_ROOM_IDENTIFIER_REGEX = "^![\\s\\S]+$DOMAIN_REGEX$"
    private val PATTERN_CONTAIN_M_ROOM_IDENTIFIER =
        M_ROOM_IDENTIFIER_REGEX.toRegex()

    private const val M_ROOM_IDENTIFIER_DOMAINLESS_REGEX = "!$BASE_64_URL_SAFE_ALPHABET"
    private val PATTERN_CONTAIN_MATRIX_ROOM_IDENTIFIER_DOMAINLESS =
        M_ROOM_IDENTIFIER_DOMAINLESS_REGEX.toRegex()

    // Room aliases
    private const val M_ROOM_ALIAS_REGEX = "^#\\S+$DOMAIN_REGEX$"
    private val PATTERN_CONTAIN_M_ALIAS =
        M_ROOM_ALIAS_REGEX.toRegex(RegexOption.IGNORE_CASE)

    // Event IDs
    private const val M_EVENT_IDENTIFIER_REGEX = "^\\$[\\s\\S]+$DOMAIN_REGEX$"
    private val PATTERN_CONTAIN_MATRIX_EVENT_IDENTIFIER =
        M_EVENT_IDENTIFIER_REGEX.toRegex()

    // Event IDs v3/v4 (base64/url-safe base64)
    private const val M_EVENT_IDENTIFIER_V3_REGEX = "\\$$BASE_64_ALPHABET"
    private val PATTERN_CONTAIN_MATRIX_EVENT_IDENTIFIER_V3 =
        M_EVENT_IDENTIFIER_V3_REGEX.toRegex()

    private const val M_EVENT_IDENTIFIER_V4_REGEX = "\\$$BASE_64_URL_SAFE_ALPHABET"
    private val PATTERN_CONTAIN_M_EVENT_IDENTIFIER_V4 =
        M_EVENT_IDENTIFIER_V4_REGEX.toRegex()

    private const val MAX_IDENTIFIER_LENGTH = 255

    fun isUserId(str: String?): Boolean {
        return str != null &&
                str.length <= MAX_IDENTIFIER_LENGTH &&
                str matches PATTERN_CONTAIN_M_USER_IDENTIFIER
    }

    fun isSpaceId(str: String?) = isRoomId(str)

    fun isRoomId(str: String?): Boolean {
        return str != null &&
                str.length <= MAX_IDENTIFIER_LENGTH &&
                (str matches PATTERN_CONTAIN_MATRIX_ROOM_IDENTIFIER_DOMAINLESS ||
                        str matches PATTERN_CONTAIN_M_ROOM_IDENTIFIER)
    }

    fun isRoomAlias(str: String?): Boolean {
        return str != null &&
                str.length <= MAX_IDENTIFIER_LENGTH &&
                str matches PATTERN_CONTAIN_M_ALIAS
    }

    fun isEventId(str: String?): Boolean {
        return str != null &&
                str.length <= MAX_IDENTIFIER_LENGTH &&
                (str matches PATTERN_CONTAIN_M_EVENT_IDENTIFIER_V4 ||
                        str matches PATTERN_CONTAIN_MATRIX_EVENT_IDENTIFIER_V3 ||
                        str matches PATTERN_CONTAIN_MATRIX_EVENT_IDENTIFIER)
    }

    // Thread IDs follow event ID rules
    fun isThreadId(str: String?) = isEventId(str)

    /**
     * Scans text and extracts Matrix IDs/aliases and known permalink forms.
     */
    fun findPatterns(text: CharSequence, permalinkParser: PermalinkParser): List<MPatternResult> {
        val rawTextMatches = "\\S+$DOMAIN_REGEX".toRegex(RegexOption.IGNORE_CASE).findAll(text)
        val urlMatches = "\\[\\S+\\]\\((\\S+)\\)".toRegex(RegexOption.IGNORE_CASE).findAll(text)
        val atRoomMatches = Regex("@room").findAll(text)

        return buildList {
            for (match in rawTextMatches) {
                val type = when {
                    isUserId(match.value) -> MPatternType.USER_ID
                    isRoomId(match.value) -> MPatternType.ROOM_ID
                    isRoomAlias(match.value) -> MPatternType.ROOM_ALIAS
                    isEventId(match.value) -> MPatternType.EVENT_ID
                    else -> null
                }
                if (type != null) {
                    add(MPatternResult(type, match.value, match.range.first, match.range.last + 1))
                }
            }
            for (match in urlMatches) {
                val url = match.groupValues[1]
                when (val permalink = permalinkParser.parse(url)) {
                    is PermalinkData.UserLink -> {
                        add(MPatternResult(MPatternType.USER_ID, permalink.userId.toString(), match.range.first, match.range.last + 1))
                    }
                    is PermalinkData.RoomLink -> {
                        when (permalink.roomIdOrAlias) {
                            is RoomIdOrAlias.Alias -> MPatternType.ROOM_ALIAS
                            is RoomIdOrAlias.Id -> if (permalink.eventId == null) MPatternType.ROOM_ID else null
                        }?.let { type ->
                            add(MPatternResult(type, permalink.roomIdOrAlias.identifier, match.range.first, match.range.last + 1))
                        }
                    }
                    else -> Unit
                }
            }
            for (match in atRoomMatches) {
                add(MPatternResult(MPatternType.AT_ROOM, match.value, match.range.first, match.range.last + 1))
            }
        }
    }
}

enum class MPatternType {
    USER_ID,
    ROOM_ID,
    ROOM_ALIAS,
    EVENT_ID,
    AT_ROOM
}

data class MPatternResult(
    val type: MPatternType,
    val value: String,
    val start: Int,
    val end: Int,
)