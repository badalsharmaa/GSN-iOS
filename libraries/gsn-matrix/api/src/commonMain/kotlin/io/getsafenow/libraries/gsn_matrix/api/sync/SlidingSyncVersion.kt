package io.getsafenow.libraries.gsn_matrix.api.sync

sealed interface SlidingSyncVersion {
    data object None : SlidingSyncVersion
    data object Proxy : SlidingSyncVersion
    data object Native : SlidingSyncVersion
}
