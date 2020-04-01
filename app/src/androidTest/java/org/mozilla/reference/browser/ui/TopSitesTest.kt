/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ui

import android.net.Uri
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mozilla.reference.browser.helpers.AndroidAssetDispatcher
import org.mozilla.reference.browser.helpers.BrowserActivityTestRule
import org.mozilla.reference.browser.helpers.TestAssetHelper
import org.mozilla.reference.browser.ui.robots.freshTab
import org.mozilla.reference.browser.ui.robots.navigationToolbar

class TopSitesTest {

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
    fun topSitesTest() {
        val defaultWebPage = TestAssetHelper.getGenericAsset(mockWebServer, 1)
        createTopSite(defaultWebPage.url, defaultWebPage.content)
        val title = "localhost"

        // Verify a top site was created.
        navigationToolbar {
        }.openThreeDotMenu {
        }.openNewTab {
            verifyTopSite(title)
        }

        // open the top site
        freshTab {
        }.openTopSite(title) {
            verifyPageContent(defaultWebPage.content)
        }.openNavigationToolbar {
        }.openThreeDotMenu {
            verifyThreeDotMenuExists()
        }.openNewTab {
        }

        // open the top site in a new tab
        freshTab {
        }.openTopSiteInNewTab(title) {
            verifyPageContent(defaultWebPage.content)
        }.openNavigationToolbar {
        }.openThreeDotMenu {
            verifyThreeDotMenuExists()
        }.openNewTab {
        }

        // hide the top site from new tab
        freshTab {
        }.removeTopSite(title) {
            verifyTopSiteDoesNotExist(title)
        }
    }

    private fun createTopSite(uri: Uri, content: String) {
        navigationToolbar {
        }.freshTabEnterUrlAndEnterToBrowser(uri) {
            verifyPageContent(content)
        }
        navigationToolbar {
        }.enterUrlAndEnterToBrowser(uri) {
            verifyPageContent(content)
        }
        navigationToolbar {
        }.enterUrlAndEnterToBrowser(uri) {
            verifyPageContent(content)
        }
    }
}
