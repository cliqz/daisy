/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.helpers

import android.net.Uri
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.mozilla.reference.browser.ext.waitAndInteract
import org.mozilla.reference.browser.ui.robots.mDevice

object TestHelper {

    /**
     * Blocks the test till it finds the view and then performs long click on it.
     */
    fun longTapSelectItem(url: Uri) {
        mDevice.waitAndInteract(Until.findObject(By.text(url.toString()))) {}
        onView(withText(url.toString())).perform(longClick())
    }
}
