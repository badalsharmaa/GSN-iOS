package io.getsafenow.libraries.gsn_matrix.api.user

import kotlinx.collections.immutable.ImmutableList

data class MSearchUserResults(
    val results: ImmutableList<GsnMUser>,
    val limited: Boolean,
)
