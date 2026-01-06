package io.getsafenow.libraries.gsn_matrix.api.room

import io.getsafenow.libraries.gsn_matrix.api.core.UserId


sealed interface IntentionalMention {
    data class User(val userId: UserId) : IntentionalMention
    data object Room : IntentionalMention
}
