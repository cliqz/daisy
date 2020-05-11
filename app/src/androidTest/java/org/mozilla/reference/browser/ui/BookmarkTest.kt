/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.reference.browser.ui

import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mozilla.reference.browser.helpers.AndroidAssetDispatcher
import org.mozilla.reference.browser.helpers.BrowserActivityTestRule
import org.mozilla.reference.browser.helpers.TestAssetHelper
import org.mozilla.reference.browser.helpers.TestHelper.longTapSelectItem
import org.mozilla.reference.browser.ui.robots.multipleSelectionToolbar
import org.mozilla.reference.browser.ui.robots.navigationToolbar

class BookmarkTest {

    private lateinit var mockWebServer: MockWebServer

    @get:Rule
    val activityTestRule = BrowserActivityTestRule()

    @Before
    fun setUp() {
        mockWebServer = MockWebServer().apply {
            setDispatcher(AndroidAssetDispatcher())
            start()
        }
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun noBookmarkItemsTest() {
        navigationToolbar {
        }.openThreeDotMenu {
            verifyBookmarksButton()
        }.openBookmarks {
            verifyEmptyBookmarkView()
        }
    }

    @Test
    fun addToBookmarksTest() {
        val defaultWebPage = TestAssetHelper.getGenericAsset(mockWebServer, 1)
        navigationToolbar {
            createBookmark(defaultWebPage.url)
        }.openThreeDotMenu {
        }.openBookmarks {
            verifyBookmarkItemExists(defaultWebPage.url.toString())
        }
    }

    @Test
    fun deleteBookmarkItemTest() {
        val defaultWebPage = TestAssetHelper.getGenericAsset(mockWebServer, 1)
        navigationToolbar {
            createBookmark(defaultWebPage.url)
        }.openThreeDotMenu {
        }.openBookmarks {
            clickBookmarkItemDelete()
            verifyEmptyBookmarkView()
        }
    }

    @Test
    fun openBookmarkItemTest() {
        val defaultWebPage = TestAssetHelper.getGenericAsset(mockWebServer, 1)
        navigationToolbar {
            createBookmark(defaultWebPage.url)
        }.openThreeDotMenu {
        }.openBookmarks {
            verifyBookmarkItemExists(defaultWebPage.url.toString())
        }.openBookmarkUrl(defaultWebPage.url) {
            verifyCustomUrl(defaultWebPage.url.toString())
        }
    }

    @Test
    fun multiSelectionToolbarItemsTest() {
        val defaultWebPage = TestAssetHelper.getGenericAsset(mockWebServer, 1)
        navigationToolbar {
            createBookmark(defaultWebPage.url)
        }.openThreeDotMenu {
        }.openBookmarks {
            longTapSelectItem(defaultWebPage.url)
        }

        multipleSelectionToolbar {
            verifyMultiSelectionCheckmark()
            verifyMultiSelectionCounter()
            verifyCloseToolbarButton()
        }.closeToolbarReturnToBookmarks {
            verifyBookmarkViewExists()
        }
    }

    @Test
    fun deleteMultipleSelectionTest() {
        val firstWebPage = TestAssetHelper.getGenericAsset(mockWebServer, 1)
        val secondWebPage = TestAssetHelper.getGenericAsset(mockWebServer, 2)

        navigationToolbar {
            createBookmark(firstWebPage.url)
        }.openThreeDotMenu {
        }.openNewTab {
        }
        navigationToolbar {
            createBookmark(secondWebPage.url)
        }.openThreeDotMenu {
        }.openBookmarks {
            longTapSelectItem(firstWebPage.url)
            longTapSelectItem(secondWebPage.url)
        }

        multipleSelectionToolbar {
        }.clickBookmarksMultiSelectionDelete {
            verifyBookmarkViewExists()
        }
    }
}
