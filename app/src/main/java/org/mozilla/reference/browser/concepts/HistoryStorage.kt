package org.mozilla.reference.browser.concepts

import mozilla.components.concept.storage.BookmarkNode
import mozilla.components.concept.storage.HistoryStorage
import mozilla.components.concept.storage.SearchResult
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

    /**
     * Gets all the bookmarks.
     * @return a list of [BookmarkNode]
     */
    suspend fun getBookmarks(): List<BookmarkNode>

    /**
     * Adds a new bookmark item.
     * @param url the url to bookmark.
     * @param title title of the url.
     * @param parentId parent id of the new item. Default is 0 which means add the item to root.
     * @return The id of the newly created bookmark item.
     */
    suspend fun addBookmark(url: String, title: String, parentId: Int = 0): Int

    /**
     * Adds a new bookmark folder.
     * @param title title of the url.
     * @param parentId parent id of the new item. Default is 0 which means add the folder to root.
     * @return The id of the newly created bookmark folder.
     */
    suspend fun addFolder(title: String, parentId: Int = 0): Int

    /**
     * Gets a bookmark with url.
     * @param url the url to check.
     * @return A bookmark that matches the url.
     */
    suspend fun getBookmarkWithUrl(url: String): BookmarkNode?

    /**
     * Deletes the bookmark.
     * @return True on deleting the bookmark.
     */
    suspend fun deleteBookmark(id: Int): Boolean

    /**
     * Searches bookmark.
     * @param query the query string to search.
     * @return The list of bookmarks.
     */
    fun searchBookmarks(query: String): List<SearchResult>
}
