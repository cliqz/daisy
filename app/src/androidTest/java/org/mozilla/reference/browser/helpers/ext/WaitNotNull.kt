/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.helpers.ext

import androidx.test.uiautomator.SearchCondition
import androidx.test.uiautomator.UiDevice
import org.junit.Assert
import org.mozilla.reference.browser.helpers.TestAssetHelper

/**
 * Borrowed from the Fenix project
 *
 * Blocks the test for [waitTime] milliseconds before continuing.
 *
 * Will cause the test to fail is the condition is not met before the timeout.
 */
fun UiDevice.waitNotNull(
    searchCondition: SearchCondition<*>,
    waitTime: Long = TestAssetHelper.waitingTimeShort
) = Assert.assertNotNull(wait(searchCondition, waitTime))