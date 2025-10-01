package io.getsafenow.libraries.gsn_matrix.api.encryption

enum class RecoveryState {
    /**
     * Special value, when the SDK is waiting for the first sync to be done.
     */
    WAITING_FOR_SYNC,

    /**
     * Values mapped from the SDK.
     */
    UNKNOWN,
    ENABLED,
    DISABLED,
    INCOMPLETE,
}
