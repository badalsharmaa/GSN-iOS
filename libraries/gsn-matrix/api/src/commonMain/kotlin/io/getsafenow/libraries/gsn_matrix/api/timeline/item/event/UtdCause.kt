package io.getsafenow.libraries.gsn_matrix.api.timeline.item.event

enum class UtdCause {
    Unknown,
    SentBeforeWeJoined,
    VerificationViolation,
    UnsignedDevice,
    UnknownDevice,

    /**
     * We are missing the keys for this event, but it is a "device-historical" message and
     * there is no key storage backup on the server, presumably because the user has turned it off.
     */
    HistoricalMessageAndBackupIsDisabled,

    /**
     * We are missing the keys for this event, but it is a "device-historical"
     * message, and even though a key storage backup does exist, we can't use
     * it because our device is unverified.
     */
    HistoricalMessageAndDeviceIsUnverified,

    /**
     * The key was withheld on purpose because your device is insecure and/or the
     * sender trust requirement settings are not met for your device.
     */
    WithheldUnverifiedOrInsecureDevice,

    /**
     * Key is withheld by sender.
     */
    WithheldBySender,
}
