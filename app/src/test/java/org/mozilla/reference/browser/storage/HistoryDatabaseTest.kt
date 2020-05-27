/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.reference.browser.storage

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runBlockingTest
import mozilla.components.concept.storage.PageVisit
import mozilla.components.concept.storage.RedirectSource
import mozilla.components.concept.storage.VisitType
import mozilla.components.support.test.robolectric.testContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class HistoryDatabaseTest {

    private lateinit var dbFile: File
    private lateinit var db: HistoryDatabase

    @Before
    fun setUp() {
        dbFile = testContext.filesDir
            .resolve("../databases")
            .resolve(HistoryDatabase.DATABASE_NAME)
        db = HistoryDatabase(testContext)
    }

    @After
    fun tearDown() {
        dbFile.delete()
    }

    @Test
    fun `should create the db file`() {
        assertTrue("The database file should exists", dbFile.exists())
    }

    @Test
    fun `should record a visit and recall it`() = runBlockingTest {
        val testUri = "https://cliqz.com"
        db.recordVisit(testUri, PageVisit(VisitType.TYPED, RedirectSource.NOT_A_SOURCE))
        val results = db.getVisitsPaginated(0, 1000, listOf())
        assertEquals("Should have only a visit", 1, results.size)
        assertEquals("The uri should be the same", testUri, results[0].url)
    }

    @Test
    fun `should return true for visited pages`() = runBlockingTest {
        val testUris = listOf("https://cliqz.com", "https://facebook.com")
        testUris.forEach {
            db.recordVisit(it, PageVisit(VisitType.TYPED, RedirectSource.NOT_A_SOURCE))
        }
        assertTrue("All the test uris should be visited", db.getVisited().containsAll(testUris))
        val visited = db.getVisited(testUris)
        assertEquals("getVisited(list) should return exactly a list with the same number of elements",
            testUris.size, visited.size)
        assertTrue("All the test uris should be visited", visited.all { it })
    }

    @Test
    fun `should return false for not visited pages`() = runBlockingTest {
        assertFalse("cliqz.com should not have been visited",
            db.getVisited(listOf("https://facebook.com")).all { it })
    }

    @Test
    fun `should record bookmarks`() = runBlockingTest {
        val rowId = db.addItem("0", "https://cliqz.com", "Cliqz", null)
        assertEquals("1", rowId)

        val bookmarks = db.getTree("0")
        assertNotNull(bookmarks)
        assertNotNull(bookmarks?.children)
        assertEquals(1, bookmarks?.children?.size)
        assertEquals("Cliqz", bookmarks?.children?.first()?.title)
        assertEquals("https://cliqz.com", bookmarks?.children?.first()?.url)
    }
}