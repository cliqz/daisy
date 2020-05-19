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
import mozilla.components.concept.storage.BookmarkNode
import mozilla.components.concept.storage.BookmarkNodeType
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
import java.util.Locale

/**
 * Ported the class from Cliqz browser. This class implements the HistoryStorage interface
 * which makes it compatible with the Mozilla components.
 * The implemented methods are wired to the existing methods of the class.
 */
@Suppress("LargeClass", "TooManyFunctions")
class HistoryDatabase(context: Context) :
    SQLiteOpenHelper(context.applicationContext, DATABASE_NAME, null, DATABASE_VERSION),
    HistoryStorage {

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
    private object UrlsTable {
        const val TABLE_NAME = "urls"
        // Columns
        const val ID = "id"
        const val URL = "url"
        const val DOMAIN = "domain" // Added in v6
        const val TITLE = "title"
        const val VISITS = "visits"
        const val TIME = "time"
        const val FAVORITE = "favorite"
        const val FAV_TIME = "fav_time"
    }

    private object HistoryTable {
        const val TABLE_NAME = "history"
        // Columns
        const val ID = "id"
        const val URL_ID = "url_id"
        const val TIME = "time"
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
            createV9DB(db)
        }
    }

    private fun createV4DB(db: SQLiteDatabase) {
        db.execSQL(res.getString(R.string.create_urls_table_v4))
        db.execSQL(res.getString(R.string.create_history_table_v4))
        db.execSQL(res.getString(R.string.create_urls_index_v4))
        db.execSQL(res.getString(R.string.create_visits_index_v4))
    }

    private fun createV9DB(db: SQLiteDatabase) {
        db.execSQL(res.getString(R.string.create_urls_table_v6))
        db.execSQL(res.getString(R.string.create_history_table_v5))
        db.execSQL(res.getString(R.string.create_urls_index_v5))
        db.execSQL(res.getString(R.string.create_visits_index_v5))
        db.execSQL(res.getString(R.string.create_blocked_topsites_table_v6))
        db.execSQL(res.getString(R.string.create_queries_table_v7))
        db.execSQL(res.getString(R.string.create_history_time_index_v8))
        db.execSQL(res.getString(R.string.create_url_index_v9))
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
            else {
                execSQL("DROP TABLE IF EXISTS " + UrlsTable.TABLE_NAME)
                execSQL("DROP TABLE IF EXISTS " + HistoryTable.TABLE_NAME)
                execSQL("DROP TABLE IF EXISTS " + BlockedTopSitesTable.TABLE_NAME)
                execSQL("DROP TABLE IF EXISTS " + QueriesTable.TABLE_NAME)
                // Create tables again
                createV9DB(this)
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
        urlsValues.put(UrlsTable.TITLE, title)
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

    @get:Synchronized
    val favorites: List<BookmarkNode>
        get() {
            val results = mutableListOf<BookmarkNode>()
            val db = dbHandler.database ?: return results
            val cursor = db.rawQuery(res.getString(R.string.get_favorite_query_v5), null)
            cursor.use {
                if (cursor.moveToFirst()) {
                    val urlIndex = cursor.getColumnIndex(UrlsTable.URL)
                    val titleIndex = cursor.getColumnIndex(UrlsTable.TITLE)
                    do {
                        results.add(BookmarkNode(
                            type = BookmarkNodeType.ITEM,
                            guid = "", // We need to fetch the id from the DB
                            parentGuid = null,
                            title = cursor.getString(titleIndex),
                            url = cursor.getString(urlIndex),
                            position = null,
                            children = null
                        ))
                    } while (cursor.moveToNext())
                }
            }
            return results
        }

    @Suppress("unused")
    @Synchronized
    fun isFavorite(url: String): Boolean {
        val db = dbHandler.database ?: return false
        val cursor = db.query(UrlsTable.TABLE_NAME, arrayOf(UrlsTable.ID),
            String.format(Locale.US, "%s=? AND %s=1", UrlsTable.URL, UrlsTable.FAVORITE), arrayOf(url),
            null, null, null)
        val result = cursor.count > 0
        cursor.close()
        return result
    }

    @Suppress("unused")
    @Synchronized
    fun setFavorites(url: String, title: String?, favTime: Long, isFavorite: Boolean) {
        val db = dbHandler.database ?: return
        val values = ContentValues()
        values.put(UrlsTable.FAVORITE, isFavorite)
        values.put(UrlsTable.FAV_TIME, favTime)
        val cursor = db.rawQuery(res.getString(R.string.search_url_v5), arrayOf(url))
        db.beginTransaction {
            if (cursor.count > 0) {
                update(UrlsTable.TABLE_NAME, values, "url = ?", arrayOf(url))
            } else {
                values.put(UrlsTable.URL, url)
                values.put(UrlsTable.TITLE, title)
                values.put(UrlsTable.VISITS, 0)
                insert(UrlsTable.TABLE_NAME, null, values)
            }
        }
        cursor.close()
    }

    override suspend fun getBookmarks() = favorites

    override suspend fun addBookmark(url: String, title: String) {
        setFavorites(url, title, System.currentTimeMillis(), true)
    }

    override suspend fun isBookmark(url: String): Boolean {
        return isFavorite(url)
    }

    override suspend fun deleteBookmark(url: String) {
        setFavorites(url, null, System.currentTimeMillis(), false)
    }

    override fun searchBookmarks(query: String): List<SearchResult> {
        val db = dbHandler.database ?: return listOf()
        val formattedSearch = String.format("%%%s%%", query)
        val selectQuery = res.getString(R.string.search_favorite_query)
        val cursor = db.rawQuery(selectQuery, arrayOf(formattedSearch, formattedSearch))

        val bookmarkSuggestions = mutableListOf<SearchResult>()
        if (cursor.moveToFirst()) {
            do {
                val url = cursor.getString(cursor.getColumnIndex(UrlsTable.URL))
                val title = cursor.getString(cursor.getColumnIndex(UrlsTable.TITLE))
                // The bookmark query we use does not return any 'score' attribute column
                val bookmarkSuggestion = SearchResult(url, url, 0, title)
                bookmarkSuggestions.add(bookmarkSuggestion)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return bookmarkSuggestions
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
            val visits = cursor.getLong(cursor.getColumnIndex(UrlsTable.VISITS)) - 1
            val favorite = cursor.getInt(cursor.getColumnIndex(UrlsTable.FAVORITE)) > 0
            db.beginTransaction {
                delete(HistoryTable.TABLE_NAME, "id=?", arrayOf(id.toString()))
                if (visits <= 0 && !favorite) {
                    delete(UrlsTable.TABLE_NAME, "id=?", arrayOf(uid.toString()))
                } else {
                    val value = ContentValues()
                    value.put(UrlsTable.VISITS, if (visits < 0) 0 else visits)
                    update(UrlsTable.TABLE_NAME, value, "id=?", arrayOf(uid.toString()))
                }
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
                val contentValues = ContentValues()
                contentValues.put(UrlsTable.FAVORITE, false)
                update(UrlsTable.TABLE_NAME, contentValues, null, null)
                // delete all entries with visits = 0;
                delete(UrlsTable.TABLE_NAME, "visits=0", null)
            } else { // empty history table
                delete(HistoryTable.TABLE_NAME, null, null)
                // delete rows where favorite != 1
                delete(UrlsTable.TABLE_NAME, "favorite<1", null)
                // update "visits" of remaining rows to 0
                val contentValues = ContentValues()
                contentValues.put(UrlsTable.VISITS, 0)
                update(UrlsTable.TABLE_NAME, contentValues, null, null)
            }
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
        private const val MAX_TOP_SITE_LIMIT = 100
        private const val DATABASE_VERSION = 9
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
