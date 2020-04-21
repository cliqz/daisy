package org.mozilla.reference.browser.concepts

import mozilla.components.concept.storage.HistoryStorage
import mozilla.components.concept.storage.SearchResult
import org.json.JSONArray
import org.mozilla.reference.browser.storage.model.TopSite

/**
 * An interface which extends the components HistoryStorage with the specific top sites and bookmarks
 * support methods
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

    suspend fun getBookmarks(): JSONArray

    suspend fun addBookmark(url: String, title: String)

    suspend fun isBookmark(url: String): Boolean

    suspend fun deleteBookmark(url: String)

    fun searchBookmarks(query: String): List<SearchResult>
}
