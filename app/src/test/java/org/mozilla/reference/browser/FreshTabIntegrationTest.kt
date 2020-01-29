/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser

import android.view.View
import com.cliqz.browser.freshtab.FreshTab
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import mozilla.components.concept.engine.EngineView
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mozilla.reference.browser.browser.FreshTabIntegration

@ExperimentalCoroutinesApi
class FreshTabIntegrationTest {

    /**
     * Derived from AwesomeBarFeatureTest
     */
    @Test
    fun `FreshTabIntegration connects Toolbar and EngineView with FreshTab`() = runBlockingTest {
        val freshTab: FreshTab = mockk()
        val engineView: EngineView = mockk()

        var freshTabVisibility: Int? = null
        every { freshTab.visibility = any() } answers {
            freshTabVisibility = args[0] as Int
        }

        var engineViewVisibility: Int? = null
        every { engineView.asView().visibility = any() } answers {
            engineViewVisibility = args[0] as Int
        }

        val freshTabIntegration = FreshTabIntegration(
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockk(relaxed = true),
            freshTab,
            engineView,
            mockk(relaxed = true)
        )

        assertTrue(freshTabVisibility == View.VISIBLE)
        assertTrue(engineViewVisibility == View.GONE)

        every { freshTabIntegration.currentUrl } answers { "www.cliqz.com" }

        freshTabIntegration.updateVisibility()

        assertTrue(freshTabVisibility == View.GONE)
        assertTrue(engineViewVisibility == View.VISIBLE)
    }
}
