package io.getsafenow.libraries.gsn_matrix.api.permalink

import io.getsafenow.libraries.kmputils.platformkmp.PlatformUri


/**
 * Maps an input URL (e.g., a GetSafeNow Web permalink) to a matrix.to-compliant URL.
 *
 * Examples:
 * - https://web.getsafenow.app/#/room/#incident-ops:getsafenow.org  ->  https://matrix.to/#/#incident-ops:getsafenow.org
 * - https://dev.getsafenow.app/#/room/#dispatch:getsafenow.org      ->  https://matrix.to/#/#dispatch:getsafenow.org
 * - https://www.example.com/#/room/#alerts:getsafenow.org           ->  https://matrix.to/#/#alerts:getsafenow.org
 */
interface MatrixToConverter {
    /**
     * Converts a GetSafeNow (or compatible client) permalink into a matrix.to URL when possible.
     * Returns null if the input cannot be converted to a valid matrix.to URL.
     */
    fun convert(uri: PlatformUri): PlatformUri?
}