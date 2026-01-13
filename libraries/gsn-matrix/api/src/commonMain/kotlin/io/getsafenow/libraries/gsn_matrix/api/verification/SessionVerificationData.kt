package io.getsafenow.libraries.gsn_matrix.api.verification

import androidx.compose.runtime.Immutable

@Immutable
sealed interface SessionVerificationData {
    data class Emojis(
        // 7 emojis
        val emojis: List<VerificationEmoji>,
    ) : SessionVerificationData

    data class Decimals(
        // 3 numbers
        val decimals: List<Int>,
    ) : SessionVerificationData
}

data class VerificationEmoji(
    val number: Int,
)
