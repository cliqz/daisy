package org.mozilla.reference.browser.storage

import mozilla.components.concept.storage.HistoryStorage
import org.mozilla.reference.browser.storage.model.TopSite

/**
 * An interface which extends the components HistoryStorage with the specific top sites support
 * methods
 */
interface HistoryStorage : HistoryStorage {

    /**
     * Retrieves a list of top most visited websites.
     * @param limit the number of items to return
     * @return a list of [TopSite]. The time stamp of these elements is always -1.
     */
    fun getTopSites(limit: Int): List<TopSite>

    /**
     * Records the [domains] to be maintained in a 'block list'.
     * @param domains one more more domain names
     */
    fun blockDomainsForTopSites(vararg domains: String)

    /**
     * Remove domains from the 'block list' which are blocked from top sites.
     */
    fun restoreTopSites()
}
