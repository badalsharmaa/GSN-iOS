package io.getsafenow.libraries.gsn_matrix.api.timeline.item.event

import io.getsafenow.libraries.gsn_matrix.api.core.UserId


data class Receipt(
    val userId: UserId,
    val timestamp: Long,
)
