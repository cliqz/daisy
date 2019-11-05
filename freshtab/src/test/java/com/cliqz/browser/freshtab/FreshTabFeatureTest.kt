package com.cliqz.browser.freshtab

import android.view.View
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import mozilla.components.browser.session.SessionManager
import mozilla.components.browser.toolbar.BrowserToolbar
import mozilla.components.concept.engine.EngineView
import org.junit.Assert.assertTrue
import org.junit.Test

@ExperimentalCoroutinesApi
class FreshTabFeatureTest {

    /**
     * Derived from AwesomeBarFeatureTest
     */
    @Test
    fun `Feature connects Toolbar and EngineView with FreshTab`() = runBlockingTest {
        val toolbar: BrowserToolbar = mockk(relaxed = true)
        val freshTab: FreshTab = mockk()
        val engineView: EngineView = mockk()
        val sessionManager: SessionManager = mockk(relaxed = true)

        var freshTabVisibility: Int? = null
        every { freshTab.visibility = any() } answers {
            freshTabVisibility = args[0] as Int
        }

        var engineViewVisibility: Int? = null
        every { engineView.asView().visibility = any() } answers {
            engineViewVisibility = args[0] as Int
        }

        val freshTabFeature = FreshTabFeature(toolbar, freshTab, engineView, sessionManager)

        assertTrue(freshTabVisibility == View.VISIBLE)
        assertTrue(engineViewVisibility == View.GONE)

        every { freshTabFeature.currentUrl } answers { "www.cliqz.com" }

        freshTabFeature.updateVisibility()

        assertTrue(freshTabVisibility == View.GONE)
        assertTrue(engineViewVisibility == View.VISIBLE)
    }
}
