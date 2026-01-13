package io.getsafenow.libraries.gsn_matrix.api.core

import kotlin.jvm.JvmInline
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class DeviceId(val value: String) {
    override fun toString(): String = value
}
