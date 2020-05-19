/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.tabstray

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import mozilla.components.concept.tabstray.Tabs
import mozilla.components.support.test.mock
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.verify

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class TabsAdapterTest {

    @Test
    fun `itemCount will reflect number of sessions`() {
        val adapter = TabsAdapter()
        assertEquals(0, adapter.itemCount)

        adapter.updateTabs(Tabs(listOf(
            tabWithUrl("A"),
            tabWithUrl("B")), 0))
        assertEquals(2, adapter.itemCount)

        adapter.updateTabs(Tabs(listOf(
            tabWithUrl("A"),
            tabWithUrl("B"),
            tabWithUrl("C")), 0))

        assertEquals(3, adapter.itemCount)
    }

    @Test
    fun `adapter will ask holder to unbind if view gets recycled`() {
        val adapter = TabsAdapter()
        val holder: TabViewHolder = mock()

        adapter.onViewRecycled(holder)

        verify(holder).unbind()
    }
}
