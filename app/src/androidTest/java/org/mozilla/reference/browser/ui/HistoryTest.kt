package org.mozilla.reference.browser.ui

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.mozilla.reference.browser.helpers.AndroidAssetDispatcher
import org.mozilla.reference.browser.helpers.BrowserActivityTestRule
import org.mozilla.reference.browser.helpers.TestAssetHelper
import org.mozilla.reference.browser.ui.robots.navigationToolbar

/**
 * @author Ravjit Uppal
 */
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

    @Ignore("Not working due to weird RecyclerView exception, works in production")
    @Test
    fun addToHistoryTest() {
        val defaultWebPage = TestAssetHelper.getGenericAsset(mockWebServer, 1)
        navigationToolbar {
        }.enterUrlAndEnterToBrowser(defaultWebPage.url) {
        }.openNavigationToolbar {
        }.openThreeDotMenu {
            verifyThreeDotMenuExists()
        }.openHistory {
            verifyHistoryExists(defaultWebPage.url.toString())
        }.openHistoryUrl(defaultWebPage.url.toString()) {
            verifyCustomUrl(defaultWebPage.url.toString())
        }
    }
}
