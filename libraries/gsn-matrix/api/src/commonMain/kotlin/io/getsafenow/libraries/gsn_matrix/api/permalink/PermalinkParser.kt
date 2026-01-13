package io.getsafenow.libraries.gsn_matrix.api.permalink

/**
 * This class turns a uri to a [PermalinkData].
 * gsn-based domains (e.g. https://app.getsafenow.com/#/user/@chagai95:getsafenow.org) permalinks
 * or privateServer.to permalinks (e.g. https://privateServer.to/#/@chagai95:privateServer.org)
 * or client permalinks (e.g. <clientPermalinkBaseUrl>user/@chagai95:clientServer.org)
 * or getSafeNow: permalinks (e.g. getSafeNow:u/chagai95:getSafeNow.org)
 */
interface PermalinkParser {
    /**
     * Turns a uri string to a [PermalinkData].
     */
    fun parse(uriString: String): PermalinkData
}
