package io.getsafenow.libraries.gsn_matrix.api.core

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
value class TransactionId(val value: String) {
    override fun toString(): String = value
}