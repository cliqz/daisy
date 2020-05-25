/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.tabstray

import androidx.test.ext.junit.runners.AndroidJUnit4
import mozilla.components.support.test.mock
import mozilla.components.support.test.robolectric.testContext
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.verify
import org.robolectric.Shadows

@RunWith(AndroidJUnit4::class)
class BrowserTabsTrayTest {

    @Test
    fun `holders will unsubscribe if view gets detached`() {
        val adapter: TabsAdapter = mock()
        val tabsTray = BrowserTabsTray(testContext, tabsAdapter = adapter)

        val shadow = Shadows.shadowOf(tabsTray)
        shadow.callOnDetachedFromWindow()

        verify(adapter).unsubscribeHolders()
    }

    @Test
    fun `TabsTray is set on adapter`() {
        val adapter = TabsAdapter()
        val tabsTray = BrowserTabsTray(testContext, tabsAdapter = adapter)

        assertEquals(tabsTray, adapter.tabsTray)
    }
}
