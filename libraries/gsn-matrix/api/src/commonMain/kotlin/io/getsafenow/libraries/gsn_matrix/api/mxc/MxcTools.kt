package io.getsafenow.libraries.gsn_matrix.api.mxc

import me.tatarka.inject.annotations.Inject


class MxcTools @Inject constructor() {
    /**
     * Regex to match a Matrix Content (mxc://) URI.
     *
     * See: https://spec.matrix.org/v1.8/client-server-api/#matrix-content-mxc-uris
     */
    private val mxcRegex = Regex("""^mxc://([^/]+)/([^/]+)$""")

    /**
     * Sanitizes an mxcUri to be used as a relative file path.
     *
     * @param mxcUri the Matrix Content (mxc://) URI of the file.
     * @return the relative file path as "<server-name>/<media-id>" or null if the mxcUri is invalid.
     */
    fun mxcUri2FilePath(mxcUri: String): String? = mxcRegex.matchEntire(mxcUri)?.let { match ->
        buildString {
            append(match.groupValues[1])
            append("/")
            append(match.groupValues[2])
        }
    }
}
