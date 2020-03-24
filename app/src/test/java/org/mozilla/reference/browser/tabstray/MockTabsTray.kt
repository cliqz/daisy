/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.reference.browser.tabstray

import kotlinx.coroutines.CoroutineScope
import mozilla.components.browser.icons.BrowserIcons
import mozilla.components.support.test.mock
import mozilla.components.support.test.robolectric.testContext
import org.mockito.Mockito.doReturn
import org.mozilla.reference.browser.components.ThumbnailsRepository

internal fun mockTabsTray(scope: CoroutineScope): BrowserTabsTray {
    val styles: TabsTrayStyling = mock()
    val tabsTray: BrowserTabsTray = mock()
    val icons: BrowserIcons = mock()

    doReturn(styles).`when`(tabsTray).styling
    doReturn(icons).`when`(tabsTray).icons
    doReturn(ThumbnailsRepository(testContext, scope)).`when`(tabsTray).thumbnailsRepository
    return tabsTray
}