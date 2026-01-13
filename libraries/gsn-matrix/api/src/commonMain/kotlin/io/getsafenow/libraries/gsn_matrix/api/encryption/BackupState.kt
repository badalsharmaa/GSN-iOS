package io.getsafenow.libraries.gsn_matrix.api.encryption

enum class BackupState {
    /**
     * Special value, when the SDK is waiting for the first sync to be done.
     */
    WAITING_FOR_SYNC,

    /**
     * Values mapped from the SDK.
     */
    UNKNOWN,
    CREATING,
    ENABLING,
    RESUMING,
    ENABLED,
    DOWNLOADING,
    DISABLING
}
