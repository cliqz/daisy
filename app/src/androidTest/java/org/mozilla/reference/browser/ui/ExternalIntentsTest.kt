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
        val url = "https://cliqz.com"
        externalIntents {
        }.openUrlByImplicitIntent(Uri.parse(url)) {
            verifyCustomUrl(url)
        }
    }

    @Test
    fun browseAndOpenExternalLink() {
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
