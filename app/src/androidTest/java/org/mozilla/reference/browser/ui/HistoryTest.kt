/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ui

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
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

class HistoryTest {

    private val mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
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
    fun noHistoryItemsTest() {
        navigationToolbar {
        }.openThreeDotMenu {
            verifyHistoryButton()
        }.openHistory {
            verifyEmptyHistoryView()
        }
    }

    @Test
    fun addToHistoryTest() {
        val defaultWebPage = TestAssetHelper.getGenericAsset(mockWebServer, 1)
        navigationToolbar {
        }.enterUrlAndEnterToBrowser(defaultWebPage.url) {
        }.openNavigationToolbar {
        }.openThreeDotMenu {
            verifyThreeDotMenuExists()
        }.openHistory {
            verifyHistoryItemExists(defaultWebPage.url.toString())
        }.openHistoryUrl(defaultWebPage.url.toString()) {
            verifyCustomUrl(defaultWebPage.url.toString())
        }
    }

    @Test
    fun deleteHistoryItemTest() {
        val defaultWebPage = TestAssetHelper.getGenericAsset(mockWebServer, 1)
        navigationToolbar {
        }.enterUrlAndEnterToBrowser(defaultWebPage.url) {
        }.openNavigationToolbar {
        }.openThreeDotMenu {
        }.openHistory {
            clickHistoryItemDelete()
            verifyEmptyHistoryView()
        }
    }

    @Test
    fun deleteAllHistoryTest() {
        val defaultWebPage = TestAssetHelper.getGenericAsset(mockWebServer, 1)

        navigationToolbar {
        }.enterUrlAndEnterToBrowser(defaultWebPage.url) {
        }.openNavigationToolbar {
        }.openThreeDotMenu {
        }.openHistory {
            verifyHistoryItemExists(defaultWebPage.url.toString())
            verifyDeleteHistoryButtonExists()
            clickDeleteHistoryButton()
            verifyDeleteConfirmationMessage()
            clickConfirmDeleteAllHistory()
            verifyEmptyHistoryView()
        }
    }

    @Test
    fun multiSelectionToolbarItemsTest() {
        val defaultWebPage = TestAssetHelper.getGenericAsset(mockWebServer, 1)
        navigationToolbar {
        }.enterUrlAndEnterToBrowser(defaultWebPage.url) {
        }.openNavigationToolbar {
        }.openThreeDotMenu {
        }.openHistory {
            longTapSelectItem(defaultWebPage.url)
        }

        multipleSelectionToolbar {
            verifyMultiSelectionCheckmark()
            verifyMultiSelectionCounter()
            verifyCloseToolbarButton()
        }.closeToolbarReturnToHistory {
            verifyHistoryViewExists()
        }
    }

    @Test
    fun deleteMultipleSelectionTest() {
        val firstWebPage = TestAssetHelper.getGenericAsset(mockWebServer, 1)
        val secondWebPage = TestAssetHelper.getGenericAsset(mockWebServer, 2)

        navigationToolbar {
        }.enterUrlAndEnterToBrowser(firstWebPage.url) {
        }

        navigationToolbar {
        }.enterUrlAndEnterToBrowser(secondWebPage.url) {
        }.openNavigationToolbar {
        }.openThreeDotMenu {
        }.openHistory {
            longTapSelectItem(firstWebPage.url)
            longTapSelectItem(secondWebPage.url)
        }

        multipleSelectionToolbar {
        }.clickMultiSelectionDelete {
            verifyEmptyHistoryView()
        }
    }
}
