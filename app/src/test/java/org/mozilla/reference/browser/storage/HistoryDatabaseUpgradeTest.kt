/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.reference.browser.storage

import android.app.Application
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runBlockingTest
import mozilla.components.concept.storage.BookmarkNodeType
import mozilla.components.support.test.robolectric.testContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.File

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class HistoryDatabaseUpgradeTest {

    private lateinit var dbFile: File

    @Before
    fun setUp() {
        val databasesFolder = testContext.filesDir.resolve("../databases").also {
            if (!it.exists()) it.mkdirs()
        }
        dbFile = databasesFolder
            .resolve(HistoryDatabase.DATABASE_NAME)
    }

    @After
    fun tearDown() {
        dbFile.delete()
    }

    @Test
    fun upgradeFromV9toV10() = runBlockingTest {
        copyDatabaseFile(HISTORY_V9_DB)
        val db = HistoryDatabase(testContext)
        val bookmarks = db.getTree("0")
        assertNotNull(bookmarks)
        assertNotNull(bookmarks!!.children)
        val children = bookmarks!!.children!!
        assertEquals(2, children.size)
        assertEquals("https://www.bbc.co.uk/news/amp/uk-politics-52806086", children[0].url!!)
        assertEquals("https://uk.mobile.reuters.com/article/amp/idUKKBN2320HF", children[1].url!!)
        assertTrue(children.all { it.type == BookmarkNodeType.ITEM })
        assertTrue(children.all { it.title != null })
    }

    private fun copyDatabaseFile(@Suppress("SameParameterValue") dbName: String) {
        HistoryDatabaseUpgradeTest::class.java.getResourceAsStream("/$dbName")?.use { input ->
            dbFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    companion object {
        const val HISTORY_V9_DB = "history_V9.sqlite"
    }
}