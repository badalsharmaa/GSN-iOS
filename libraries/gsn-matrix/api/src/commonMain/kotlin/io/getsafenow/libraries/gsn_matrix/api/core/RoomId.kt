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
value class RoomId(val value: String)  {
    init {
        if (isInDebug && !MPatternsGsn.isRoomId(value)) {
            error("`$value` is not a valid room id.\n Example room id: `!room_id:domain`.")
        }
    }

    override fun toString(): String = value
}
