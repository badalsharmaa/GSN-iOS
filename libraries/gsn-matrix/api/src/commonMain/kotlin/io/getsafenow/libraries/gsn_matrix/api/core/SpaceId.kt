/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.getsafenow.libraries.gsn_matrix.api.core

import io.getsafenow.libraries.kmputils.metadata.isInDebug
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline


@JvmInline
@Serializable
value class SpaceId(val value: String) {
    init {
        if (isInDebug && !MPatternsGsn.isSpaceId(value)) {
            error(
                "`$value` is not a valid space id.\n" +
                    "Space ids are the same as room ids.\n" +
                    "Example space id: `!space_id:domain`."
            )
        }
    }

    override fun toString(): String = value
}

/**
 * Value to use when no space is selected by the user.
 */
val MAIN_SPACE = SpaceId("!mainSpace:local")
