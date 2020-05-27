/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This class is derived from github.com/anthonycr/Lightning-Browser
 */

package org.mozilla.reference.browser.storage

import android.content.ContentValues
import android.content.Context
import android.content.res.Resources
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import android.util.Log
import androidx.core.database.getStringOrNull
import mozilla.components.concept.storage.BookmarkInfo
import mozilla.components.concept.storage.BookmarkNode
import mozilla.components.concept.storage.BookmarkNodeType
import mozilla.components.concept.storage.BookmarksStorage
import mozilla.components.concept.storage.HistoryAutocompleteResult
import mozilla.components.concept.storage.PageObservation
import mozilla.components.concept.storage.PageVisit
import mozilla.components.concept.storage.SearchResult
import mozilla.components.concept.storage.VisitInfo
import mozilla.components.concept.storage.VisitType
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.concepts.HistoryStorage
import org.mozilla.reference.browser.ext.beginTransaction
import org.mozilla.reference.browser.storage.model.TopSite
import java.net.URI

/**
 * Ported the class from Cliqz browser. This class implements the HistoryStorage interface
 * which makes it compatible with the Mozilla components.
 * The implemented methods are wired to the existing methods of the class.
 */
@Suppress("LargeClass", "TooManyFunctions")
class HistoryDatabase(context: Context) :
    SQLiteOpenHelper(context.applicationContext, DATABASE_NAME, null, DATABASE_VERSION),
    HistoryStorage, BookmarksStorage {

    override suspend fun warmUp() {
        // Nothing to do here
    }

    override suspend fun deleteEverything() {
        clearHistory(false)
    }

    override suspend fun deleteVisit(url: String, timestamp: Long) {
        deleteHistoryPoint(url, timestamp)
    }

    override suspend fun deleteVisitsBetween(startTime: Long, endTime: Long) = Unit

    override suspend fun deleteVisitsFor(url: String) = Unit

    override suspend fun deleteVisitsSince(since: Long) = Unit

    override fun getAutocompleteSuggestion(query: String): HistoryAutocompleteResult? = null

    override suspend fun getDetailedVisits(start: Long, end: Long, excludeTypes: List<VisitType>): List<VisitInfo> {
        return listOf()
    }

    override fun getSuggestions(query: String, limit: Int): List<SearchResult> {
        val db = dbHandler.database ?: return listOf()
        val formattedSearch = String.format("%%%s%%", query.trim())
        val selectQuery = res.getString(R.string.seach_history_query_v5)
        val cursor = db.rawQuery(selectQuery, arrayOf(formattedSearch, formattedSearch, limit.toString()))

        val searchSuggestions = mutableListOf<SearchResult>()
        val resultCount = 0

        if (cursor.moveToFirst()) {
            do {
                val url = cursor.getString(cursor.getColumnIndex(UrlsTable.URL))
                val title = cursor.getString(cursor.getColumnIndex(UrlsTable.TITLE))
                // The search query we use right now does not return any 'score' attribute column
                val searchSuggestion = SearchResult(url, url, 0, title)
                searchSuggestions.add(searchSuggestion)
            } while (cursor.moveToNext() && resultCount < limit)
        }
        cursor.close()
        return searchSuggestions
    }

    @Suppress("NestedBlockDepth")
    override suspend fun getVisited(): List<String> = dbHandler.database?.let { db ->
        val result = mutableListOf<String>()
        db.query(
            UrlsTable.TABLE_NAME,
            arrayOf(UrlsTable.URL),
            null, null, null, null, null)
        .use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(UrlsTable.URL)
                do {
                    result.add(it.getString(index))
                } while (it.moveToNext())
            }
        }
        result
    } ?: listOf()

    override suspend fun getVisited(uris: List<String>): List<Boolean> {
        return uris.map { isVisited(it) }
    }

    private fun isVisited(uri: String): Boolean = dbHandler.database?.let { db ->
        db
            .query(
                UrlsTable.TABLE_NAME,
                arrayOf(UrlsTable.URL),
                "${UrlsTable.URL} = ?",
                arrayOf(uri),
                null, null, null
            ).use {
                it.moveToFirst()
            }
    } ?: false

    @Synchronized
    override suspend fun getVisitsPaginated(offset: Long, count: Long, excludeTypes: List<VisitType>): List<VisitInfo> {
        val results: MutableList<VisitInfo> = ArrayList()
        val db = dbHandler.database ?: return results
        val cursor = db.rawQuery(res.getString(R.string.get_history_query_v5), arrayOf(
            count.toString(),
            offset.toString()
        ))
        if (cursor.moveToFirst()) {
            val urlIndex = cursor.getColumnIndex(UrlsTable.URL)
            val titleIndex = cursor.getColumnIndex(UrlsTable.TITLE)
            val timeIndex = cursor.getColumnIndex(HistoryTable.TIME)
            do {
                val item = VisitInfo(
                    cursor.getString(urlIndex),
                    cursor.getString(titleIndex),
                    cursor.getLong(timeIndex), VisitType.LINK)
                results.add(item)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return results
    }

    override suspend fun prune() {
    }

    // looks like this method is used for updating the metadata so we add the title to our url visits here.
    override suspend fun recordObservation(uri: String, observation: PageObservation) {
        updateMetaData(uri, observation.title)
    }

    // this method records the actual history visits
    override suspend fun recordVisit(uri: String, visit: PageVisit) {
        // open to discussion how we should handle/record redirects
        if (visit.visitType == VisitType.LINK || visit.visitType == VisitType.TYPED) {
            visitHistoryItem(uri, "untitled")
        }
    }

    override fun cleanup() {
    }

    override suspend fun runMaintenance() {
    }

    // HistoryItems table name
    @Suppress("unused")
    private object UrlsTable {
        const val TABLE_NAME = "urls"
        // Columns
        const val ID = "id"
        const val URL = "url"
        const val DOMAIN = "domain" // Added in v6
        const val TITLE = "title"
        const val VISITS = "visits"
        const val TIME = "time"
        const val FAVORITE = "favorite" // DO NOT USE THIS COLUMN, it's here as reference
        const val FAV_TIME = "fav_time" // DO NOT USE THIS COLUMN, it's here as reference
    }

    private object HistoryTable {
        const val TABLE_NAME = "history"
        // Columns
        const val ID = "id"
        const val URL_ID = "url_id"
        const val TIME = "time"
    }

    private object BookmarksTable {
        const val TABLE_NAME = "bookmarks"
        // Columns
        const val ID = "id"
        const val URL = "url"
        const val TITLE = "title"
        const val TYPE = "type"
        const val PARENT_ID = "parent_id"
    }

    private object BlockedTopSitesTable {
        const val TABLE_NAME = "blocked_topsites"
        // Columns
        const val DOMAIN = "domain"
    }

    @Suppress("unused")
    private object QueriesTable {
        const val TABLE_NAME = "queries"
        // Columns
        const val ID = "id"
        const val QUERY = "query"
        const val TIME = "time"
    }

    @Suppress("unused")
    object HistoryKeys {
        // Fields
        const val HISTORY_ID = "id"
        const val URL = "url"
        const val TITLE = "title"
        const val TIME = "timestamp"
    }

    private val res: Resources = context.resources
    private val dbHandler: DatabaseHandler = DatabaseHandler(this)

    // Creating Tables
    override fun onCreate(db: SQLiteDatabase) {
        db.beginTransaction {
            createV10DB(db)
        }
    }

    private fun createV4DB(db: SQLiteDatabase) {
        db.execSQL(res.getString(R.string.create_urls_table_v4))
        db.execSQL(res.getString(R.string.create_history_table_v4))
        db.execSQL(res.getString(R.string.create_urls_index_v4))
        db.execSQL(res.getString(R.string.create_visits_index_v4))
    }

    private fun createV10DB(db: SQLiteDatabase) {
        db.execSQL(res.getString(R.string.create_urls_table_v6))
        db.execSQL(res.getString(R.string.create_history_table_v5))
        db.execSQL(res.getString(R.string.create_urls_index_v5))
        db.execSQL(res.getString(R.string.create_visits_index_v5))
        db.execSQL(res.getString(R.string.create_blocked_topsites_table_v6))
        db.execSQL(res.getString(R.string.create_queries_table_v7))
        db.execSQL(res.getString(R.string.create_history_time_index_v8))
        db.execSQL(res.getString(R.string.create_url_index_v9))
        db.execSQL(res.getString(R.string.create_bookmarks_table))
    }

    private fun upgradeV2toV3(db: SQLiteDatabase) {
        db.execSQL(res.getString(R.string.alter_history_table_v2_to_v3))
        db.execSQL(res.getString(R.string.create_visits_index_v3))
        db.execSQL(res.getString(R.string.rename_history_table_to_tempHistory_v3_to_v4))
        db.execSQL(res.getString(R.string.drop_urlIndex_v3_to_v4))
        db.execSQL(res.getString(R.string.drop_countIndex_v3_to_v4))
        createV4DB(db)
        db.execSQL(res.getString(R.string.move_to_new_history_v3_to_v4))
        db.execSQL(res.getString(R.string.move_to_urls_v3_to_v4))
        db.execSQL(res.getString(R.string.drop_tempHistory_v3_to_v4))
        db.execSQL(res.getString(R.string.add_column_fav_time_v5))
        db.execSQL(res.getString(R.string.move_favorites_to_urls_v5))
        // Add the domain column
        db.execSQL(res.getString(R.string.add_column_domain_to_urls_v6))
        // Create the blocked topsites table
        db.execSQL(res.getString(R.string.create_blocked_topsites_table_v6))
        // create queries table
        db.execSQL(res.getString(R.string.create_queries_table_v7))
    }

    private fun upgradeV3toV4(db: SQLiteDatabase) {
        db.execSQL(res.getString(R.string.rename_history_table_to_tempHistory_v3_to_v4))
        db.execSQL(res.getString(R.string.drop_urlIndex_v3_to_v4))
        db.execSQL(res.getString(R.string.drop_countIndex_v3_to_v4))
        createV4DB(db)
        db.execSQL(res.getString(R.string.move_to_new_history_v3_to_v4))
        db.execSQL(res.getString(R.string.move_to_urls_v3_to_v4))
        db.execSQL(res.getString(R.string.drop_tempHistory_v3_to_v4))
        db.execSQL(res.getString(R.string.add_column_fav_time_v5))
        db.execSQL(res.getString(R.string.move_favorites_to_urls_v5))
        db.execSQL(res.getString(R.string.add_column_domain_to_urls_v6))
        db.execSQL(res.getString(R.string.create_blocked_topsites_table_v6))
        db.execSQL(res.getString(R.string.create_queries_table_v7))
    }

    private fun upgradeV4toV5(db: SQLiteDatabase) {
        db.execSQL(res.getString(R.string.add_column_fav_time_v5))
        db.execSQL(res.getString(R.string.move_favorites_to_urls_v5))
        db.execSQL(res.getString(R.string.add_column_domain_to_urls_v6))
        db.execSQL(res.getString(R.string.create_blocked_topsites_table_v6))
        db.execSQL(res.getString(R.string.create_queries_table_v7))
    }

    private fun upgradeV5toV6(db: SQLiteDatabase) {
        db.execSQL(res.getString(R.string.add_column_domain_to_urls_v6))
        db.execSQL(res.getString(R.string.create_blocked_topsites_table_v6))
        db.execSQL(res.getString(R.string.create_queries_table_v7))
    }

    private fun upgradeV6toV7(db: SQLiteDatabase) {
        db.execSQL(res.getString(R.string.create_queries_table_v7))
    }

    private fun upgradeV7toV8(db: SQLiteDatabase) {
        db.execSQL(res.getString(R.string.create_history_time_index_v8))
    }

    private fun upgradeV8toV9(db: SQLiteDatabase) {
        db.execSQL(res.getString(R.string.create_url_index_v9))
    }

    private fun upgradeV9toV10(db: SQLiteDatabase) {
        db.execSQL(res.getString(R.string.create_bookmarks_table))
        db.execSQL(res.getString(R.string.move_favorites_to_bookmarks_v10))
    }

    // Upgrading database
    @Suppress("ComplexMethod")
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.beginTransaction {
            if (oldVersion < V3) upgradeV2toV3(db)
            if (oldVersion < V4) upgradeV3toV4(db)
            if (oldVersion < V5) upgradeV4toV5(db)
            if (oldVersion < V6) upgradeV5toV6(db)
            if (oldVersion < V7) upgradeV6toV7(db)
            if (oldVersion < V8) upgradeV7toV8(db)
            if (oldVersion < V9) upgradeV8toV9(db)
            if (oldVersion < V10) upgradeV9toV10(db)
            else {
                execSQL("DROP TABLE IF EXISTS ${UrlsTable.TABLE_NAME}")
                execSQL("DROP TABLE IF EXISTS ${HistoryTable.TABLE_NAME}")
                execSQL("DROP TABLE IF EXISTS ${BlockedTopSitesTable.TABLE_NAME}")
                execSQL("DROP TABLE IF EXISTS ${QueriesTable.TABLE_NAME}")
                execSQL("DROP TABLE IF EXISTS ${BookmarksTable.TABLE_NAME}")
                // Create tables again
                createV10DB(this)
            }
        }
    }

    @Synchronized
    override fun close() {
        dbHandler.close()
        super.close()
    }

    @Suppress("TooGenericExceptionCaught")
    private fun updateMetaData(url: String, title: String?) {
        val db = dbHandler.database ?: return
        val q = db.query(false, UrlsTable.TABLE_NAME, arrayOf(UrlsTable.ID, UrlsTable.VISITS, UrlsTable.TIME),
            UrlsTable.URL + " = ?", arrayOf(url), null, null, null, "1")
        val urlsValues = ContentValues()
        val domain = extractDomainFrom(url)
        urlsValues.put(UrlsTable.URL, url)
        urlsValues.put(UrlsTable.DOMAIN, domain)
        if (!title.isNullOrEmpty()) {
            urlsValues.put(UrlsTable.TITLE, title)
        }
        db.beginTransaction(
            transaction = {
                if (q.count > 0) {
                    q.moveToFirst()
                    val idIndex = q.getColumnIndex(UrlsTable.ID)
                    val visitsIndex = q.getColumnIndex(UrlsTable.VISITS)
                    val timeIndex = q.getColumnIndex(UrlsTable.TIME)
                    val urlId = q.getLong(idIndex)
                    val visits = q.getLong(visitsIndex)
                    urlsValues.put(UrlsTable.VISITS, visits)
                    urlsValues.put(UrlsTable.TIME, q.getLong(timeIndex))
                    update(UrlsTable.TABLE_NAME, urlsValues, UrlsTable.ID + " = ?",
                        arrayOf(urlId.toString()))
                }
                q.close()
            },
            catchBlock = {
                Log.e("HistoryDatabase", "Error updating meta data", it)
            }
        )
    }

    /**
     * Update an history and urls
     *
     * @param url the url to update
     * @param title the title of the page to which the url is pointing
     */
    @Suppress("TooGenericExceptionCaught")
    @Synchronized
    fun visitHistoryItem(url: String, title: String?): Long {
        val db = dbHandler.database ?: return -1
        val q = db.query(false, UrlsTable.TABLE_NAME, arrayOf(UrlsTable.ID, UrlsTable.VISITS),
            UrlsTable.URL + " = ?", arrayOf(url), null, null, null, "1")
        val time = System.currentTimeMillis()
        val urlsValues = ContentValues()
        val domain = extractDomainFrom(url)
        urlsValues.put(UrlsTable.URL, url)
        urlsValues.put(UrlsTable.DOMAIN, domain)
        urlsValues.put(UrlsTable.TITLE, title)
        urlsValues.put(UrlsTable.VISITS, 1L)
        urlsValues.put(UrlsTable.TIME, time)
        return db.beginTransaction(
            transaction = {
                val historyID: Long
                val urlId: Long
                if (q.count > 0) {
                    q.moveToFirst()
                    val idIndex = q.getColumnIndex(UrlsTable.ID)
                    val visitsIndex = q.getColumnIndex(UrlsTable.VISITS)
                    urlId = q.getLong(idIndex)
                    val visits = q.getLong(visitsIndex)
                    urlsValues.put(UrlsTable.VISITS, visits + 1L)
                    db.update(UrlsTable.TABLE_NAME, urlsValues, UrlsTable.ID + " = ?",
                        arrayOf(urlId.toString()))
                } else {
                    urlId = db.insert(UrlsTable.TABLE_NAME, null, urlsValues)
                }
                q.close()
                val historyValues = ContentValues()
                historyValues.put(HistoryTable.URL_ID, urlId)
                historyValues.put(HistoryTable.TIME, time)
                historyID = db.insert(HistoryTable.TABLE_NAME, null, historyValues)
                db.setTransactionSuccessful()
                historyID
            },
            catchBlock = {
                Log.e("HistoryDatabase", "Error updating history", it)
                -1
            }
        )
    }

    /**
     * Simply delete all the entries in the blocked_topsites table
     */
    override fun restoreTopSites() {
        val db = dbHandler.database ?: return
        db.beginTransaction()
        try {
            db.delete(BlockedTopSitesTable.TABLE_NAME, null, null)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    @Suppress("NestedBlockDepth")
    @Synchronized
    override fun getTopSites(limit: Int): List<TopSite> {
        require(limit > 0) { "limit must be greater than 0" }
        require(limit <= MAX_TOP_SITE_LIMIT) { "Limit must be less or equal to $MAX_TOP_SITE_LIMIT" }
        val db = dbHandler.database ?: return listOf()
        val topSites = ArrayList<TopSite>(limit)
        val cursor = db.rawQuery(res.getString(R.string.get_top_sites_v6), null)
        var counter = 0
        if (cursor.moveToFirst()) {
            val urlIndex = cursor.getColumnIndex(UrlsTable.URL)
            val titleIndex = cursor.getColumnIndex(UrlsTable.TITLE)
            val domainIndex = cursor.getColumnIndex(UrlsTable.DOMAIN)
            val idIndex = cursor.getColumnIndex(UrlsTable.ID)
            do {
                val domain = cursor.getString(domainIndex)
                val id = cursor.getLong(idIndex)
                val url = cursor.getString(urlIndex)
                if (domain == null) {
                    val domainToCheck = extractDomainFrom(url)
                    if (domainToCheck != null) {
                        patchDomainForUrlWithId(db, id, domainToCheck)
                        if (blockedDomain(db, domainToCheck)) {
                            continue
                        }
                    }
                }
                topSites.add(TopSite(id, url, domain
                        ?: "", cursor.getString(titleIndex)))
                counter++
            } while (cursor.moveToNext() && counter < limit)
        }
        cursor.close()
        return topSites
    }

    @get:Synchronized
    val historyItemsCount: Int
        get() {
            val db = dbHandler.database ?: return 0
            val countQuery = "SELECT COUNT(*) FROM " + HistoryTable.TABLE_NAME
            val cursor = db.rawQuery(countQuery, null)
            val result = if (cursor.moveToNext()) cursor.getLong(0).toInt() else 0
            cursor.close()
            return result
        }

    @Synchronized
    fun getBookmarks(): List<BookmarkTreeNode> {
        val results = mutableListOf<BookmarkTreeNode>()
        val db = dbHandler.database ?: return results
        val cursor = db.rawQuery(res.getString(R.string.get_all_bookmarks), null)
        cursor.use {
            if (it.moveToFirst()) {
                val idIndex = it.getColumnIndex(BookmarksTable.ID)
                val urlIndex = it.getColumnIndex(BookmarksTable.URL)
                val titleIndex = it.getColumnIndex(BookmarksTable.TITLE)
                val typeIndex = it.getColumnIndex(BookmarksTable.TYPE)
                val parentIdIndex = it.getColumnIndex(BookmarksTable.PARENT_ID)
                do {
                    results.add(BookmarkTreeNode(
                        guid = it.getInt(idIndex).toString(),
                        type = if (it.getString(typeIndex) == "bookmark")
                            BookmarkNodeType.ITEM else BookmarkNodeType.FOLDER,
                        parentGuid = it.getInt(parentIdIndex).toString(),
                        title = it.getString(titleIndex),
                        url = it.getString(urlIndex),
                        position = null,
                        children = null
                    ))
                } while (it.moveToNext())
            }
        }
        return results
    }

    @Synchronized
    private fun addBookmarkNode(url: String? = null, title: String, parentId: Int, type: String): Int {
        val db = dbHandler.database ?: return 0
        val values = ContentValues()
        values.put(BookmarksTable.TITLE, title)
        url?.let {
            values.put(BookmarksTable.URL, url)
        }
        values.put(BookmarksTable.PARENT_ID, parentId)
        values.put(BookmarksTable.TYPE, type)
        var rowId = 0
        db.beginTransaction {
            rowId = insert(BookmarksTable.TABLE_NAME, null, values).toInt()
        }
        return rowId
    }

    override suspend fun addItem(parentGuid: String, url: String, title: String, position: Int?): String {
        return addBookmarkNode(
            url = url,
            title = if (title.isEmpty()) "untitled" else title,
            parentId = parentGuid.toInt(),
            type = "bookmark"
        ).toString()
    }

    override suspend fun addSeparator(parentGuid: String, position: Int?): String {
        return ""
    }

    override suspend fun addFolder(parentGuid: String, title: String, position: Int?): String {
        return addBookmarkNode(
            title = title,
            parentId = parentGuid.toInt(),
            type = "folder"
        ).toString()
    }

    override suspend fun getBookmarksWithUrl(url: String): List<BookmarkNode> {
        val db = dbHandler.database ?: return listOf()
        val query = res.getString(R.string.get_bookmark)
        val cursor = db.rawQuery(query, arrayOf(String.format("%s", url)))
        val bookmarks = mutableListOf<BookmarkNode>()
        cursor.use {
            if (it.moveToFirst()) {
                val id = it.getString(it.getColumnIndex(BookmarksTable.ID))
                val bookmarkedUrl = it.getString(it.getColumnIndex(BookmarksTable.URL))
                val title = it.getString(it.getColumnIndex(BookmarksTable.TITLE))
                val parentId = it.getString(it.getColumnIndex(BookmarksTable.PARENT_ID))
                val bookmarkNode = BookmarkNode(BookmarkNodeType.ITEM, id, parentId, 0, title, bookmarkedUrl, null)
                bookmarks.add(bookmarkNode)
            }
        }
        return bookmarks
    }

    override suspend fun getTree(guid: String, recursive: Boolean): BookmarkNode? {
        val bookmarks = getBookmarks()
        val bookmarksMap = generateTree(bookmarks)
        return bookmarksMap[guid]
    }

    private fun generateTree(bookmarks: List<BookmarkTreeNode>): Map<String, BookmarkNode> {
        val bookmarkTreeMap = mutableMapOf<String, BookmarkTreeNode>()
        for (bookmark in bookmarks) {
            bookmarkTreeMap[bookmark.guid] = bookmark
        }
        // create a root node.
        bookmarkTreeMap["0"] = BookmarkTreeNode(
            type = BookmarkNodeType.FOLDER,
            guid = "0",
            children = mutableListOf()
        )
        for (bookmark in bookmarks) {
            val parentNode = bookmarkTreeMap[bookmark.parentGuid]
            parentNode?.let {
                val children = parentNode.children ?: mutableListOf()
                children.add(bookmark)
                parentNode.children = children
            }
        }
        val bookmarkMap = mutableMapOf<String, BookmarkNode>()
        for ((key, value) in bookmarkTreeMap) {
            bookmarkMap[key] = value.toBookmarkNode()
        }
        return bookmarkMap
    }

    override suspend fun deleteNode(guid: String): Boolean {
        val db = dbHandler.database ?: return false
        var rowsAffected = 0
        db.beginTransaction {
            rowsAffected = delete(BookmarksTable.TABLE_NAME, "id=?", arrayOf(guid))
        }
        return rowsAffected > 0
    }

    override suspend fun getBookmark(guid: String): BookmarkNode? {
        return null
    }

    override suspend fun searchBookmarks(query: String, limit: Int): List<BookmarkNode> {
        val db = dbHandler.database ?: return listOf()
        val formattedSearch = String.format("%%%s%%", query)
        val selectQuery = res.getString(R.string.search_bookmarks)
        val cursor = db.rawQuery(selectQuery, arrayOf(formattedSearch, formattedSearch))

        val bookmarks = mutableListOf<BookmarkNode>()
        cursor.use {
            if (it.moveToFirst()) {
                do {
                    val id = it.getString(it.getColumnIndex(BookmarksTable.ID))
                    val url = it.getString(it.getColumnIndex(BookmarksTable.URL))
                    val title = it.getStringOrNull(it.getColumnIndex(BookmarksTable.TITLE))
                    val parentId = it.getString(it.getColumnIndex(BookmarksTable.PARENT_ID))
                    val bookmarkNode = BookmarkNode(BookmarkNodeType.ITEM, id, parentId, 0, title, url, null)
                    bookmarks.add(bookmarkNode)
                } while (it.moveToNext())
            }
        }
        return bookmarks
    }

    override suspend fun updateNode(guid: String, info: BookmarkInfo) {
        // to-do
    }

    /**
     * Delete an history point. If the history point is the last one for a given url and the url is
     * not favorite, the method will delete the url from the urls table also
     */
    @Synchronized
    @Suppress("ComplexMethod")
    fun deleteHistoryPoint(url: String, timestamp: Long) {
        val db = dbHandler.database ?: return
        val idCursor = db.rawQuery(res.getString(R.string.get_history_id_from_url_and_time),
            arrayOf(url, timestamp.toString()))
        val id = if (idCursor.moveToFirst()) {
            idCursor.getLong(idCursor.getColumnIndex(HistoryTable.ID))
        } else {
            -1
        }
        idCursor.close()
        val cursor = db.rawQuery(res.getString(R.string.get_url_from_history_id_v5),
            arrayOf(id.toString()))
        if (cursor.moveToFirst()) {
            val uid = cursor.getLong(cursor.getColumnIndex(UrlsTable.ID))
            db.beginTransaction {
                delete(HistoryTable.TABLE_NAME, "id=?", arrayOf(id.toString()))
                delete(UrlsTable.TABLE_NAME, "id=?", arrayOf(uid.toString()))
            }
        }
        cursor.close()
    }

    /**
     * Clear the history which is not favored
     *
     * @param deleteFavorites if true unfavorite the favored items
     */
    @Synchronized
    fun clearHistory(deleteFavorites: Boolean) {
        val db = dbHandler.database ?: return
        db.beginTransaction {
            if (deleteFavorites) { // mark all entries in urls table as favorite = false
                delete(BookmarksTable.TABLE_NAME, null, null)
            }
            delete(UrlsTable.TABLE_NAME, null, null)
            delete(QueriesTable.TABLE_NAME, null, null)
            // way to flush ghost entries on older sqlite version
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                execSQL(res.getString(R.string.create_temp_urls_table_v6))
                execSQL("INSERT into urls_temp SELECT * from urls")
                execSQL("drop table urls")
                execSQL("drop table queries")
                execSQL(res.getString(R.string.create_urls_table_v6))
                execSQL(res.getString(R.string.create_queries_table_v7))
                execSQL(res.getString(R.string.create_urls_index_v5))
                execSQL(res.getString(R.string.create_visits_index_v5))
                execSQL("INSERT into urls SELECT * from urls_temp")
                execSQL("drop table urls_temp")
            }
        }
    }

    @Suppress("unused")
    override fun blockDomainsForTopSites(vararg domains: String) {
        if (domains.isEmpty()) {
            return
        }
        val db = dbHandler.database ?: return
        db.beginTransaction {
            for (domain in domains) {
                val values = ContentValues()
                values.put(BlockedTopSitesTable.DOMAIN, domain)
                insert(BlockedTopSitesTable.TABLE_NAME, null, values)
            }
        }
    }

    /**
     * Removes the given domain from the list of BlockedTopSites i.e. the domain can now appear
     * again in the 'top sites' grid.
     *
     * @param domain the entry to remove from the table.
     */
    @Suppress("unused")
    fun removeDomainFromBlockedTopSites(domain: String) {
        val db = dbHandler.database ?: return
        db.beginTransaction {
            val whereClause = BlockedTopSitesTable.DOMAIN + "=?"
            delete(BlockedTopSitesTable.TABLE_NAME, whereClause, arrayOf(domain))
        }
    }

    @Suppress("unused")
    @Synchronized
    fun removeBlockedTopSites() {
        val db = dbHandler.database ?: return
        db.beginTransaction {
            if (historyItemsCount > 0) {
                delete(BlockedTopSitesTable.TABLE_NAME, null, null)
            }
        }
    }

    @Suppress("unused")
    @Synchronized
    fun addQuery(query: String?) {
        val db = dbHandler.database ?: return
        val contentValues = ContentValues()
        contentValues.put(QueriesTable.QUERY, query)
        contentValues.put(QueriesTable.TIME, System.currentTimeMillis())
        db.beginTransaction {
            insert(QueriesTable.TABLE_NAME, null, contentValues)
        }
    }

    @Suppress("unused")
    fun deleteQuery(id: Long) {
        val db = dbHandler.database ?: return
        db.beginTransaction {
            delete(QueriesTable.TABLE_NAME, "id=?", arrayOf(id.toString()))
        }
    }

    companion object {
        // All Static variables
        // Database Version
        private const val V3 = 3
        private const val V4 = 4
        private const val V5 = 5
        private const val V6 = 6
        private const val V7 = 7
        private const val V8 = 8
        private const val V9 = 9
        private const val V10 = 10
        private const val MAX_TOP_SITE_LIMIT = 100
        private const val DATABASE_VERSION = V10
        private const val DOMAIN_START_INDEX = 4
        // Database Name
        const val DATABASE_NAME = "historyManager"

        private fun extractDomainFrom(url: String): String? {
            try {
                val uri = URI.create(url)
                val host = uri.host
                return when {
                    host == null -> null
                    host.startsWith("www.") -> host.substring(DOMAIN_START_INDEX)
                    else -> host
                }
            } catch (e: IllegalArgumentException) {
                Log.e("HistoryDatabase", "Illegal url: $url", e)
            }
            return null
        }

        private fun blockedDomain(db: SQLiteDatabase, domain: String): Boolean {
            val cursor = db.query(BlockedTopSitesTable.TABLE_NAME, null, "domain = ?",
                arrayOf(domain), null, null, null)
            val result = cursor.moveToFirst()
            cursor.close()
            return result
        }

        private fun patchDomainForUrlWithId(db: SQLiteDatabase, id: Long, domain: String) {
            val domainValues = ContentValues()
            domainValues.put(UrlsTable.DOMAIN, domain)
            db.update(UrlsTable.TABLE_NAME, domainValues, UrlsTable.ID + " = ?",
                arrayOf(id.toString()))
        }
    }
}

data class BookmarkTreeNode(
    val type: BookmarkNodeType,
    val guid: String,
    val parentGuid: String? = null,
    val position: Int? = null,
    val title: String? = null,
    val url: String? = null,
    var children: MutableList<BookmarkTreeNode>? = null
)

private fun BookmarkTreeNode.toBookmarkNode(): BookmarkNode {
    val children = mutableListOf<BookmarkNode>()
    this.children?.let {
        for (node in it) {
            children.add(node.toBookmarkNode())
        }
    }
    return BookmarkNode(type, guid, parentGuid, position, title, url, children)
}
