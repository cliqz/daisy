/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ui

import android.net.Uri
import android.os.Build
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assume
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mozilla.reference.browser.helpers.AndroidAssetDispatcher
import org.mozilla.reference.browser.helpers.BrowserActivityTestRule
import org.mozilla.reference.browser.helpers.TestAssetHelper
import org.mozilla.reference.browser.ui.robots.externalIntents
import org.mozilla.reference.browser.ui.robots.navigationToolbar

class ExternalIntentsTest {

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
    fun openExternalLink() {
        Assume.assumeTrue(
                "Can only run on API > 22",
                Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1)
        val url = "https://cliqz.com"
        externalIntents {
        }.openUrlByImplicitIntent(Uri.parse(url)) {
            verifyCustomUrl(url)
        }
    }

    @Test
    fun browseAndOpenExternalLink() {
        Assume.assumeTrue(
                "Can only run on API > 22",
                Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1)
        val defaultWebPage = TestAssetHelper.getGenericAsset(mockWebServer, 1)
        navigationToolbar {
        }.enterUrlAndEnterToBrowser(defaultWebPage.url) {
        }
        val url = "https://cliqz.com"
        externalIntents {
        }.openUrlByImplicitIntent(Uri.parse(url)) {
            verifyCustomUrl(url)
        }
        navigationToolbar {
            verifyUrlBarNotFocused()
        }
    }
}
